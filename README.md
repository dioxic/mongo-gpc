# BrickLoader

## Indexes

### MongoDB Indexes

```javascript
db.brick.createIndex({ "familyCode": 1 })
db.brick.createIndex({ "classCode": 1, "attribute.code": 1 })
db.brick.createIndex({ "segmentCode": 1 })
db.brick.createIndex({ "attribute.code": 1 })
db.brick.createIndex({ "creationDate": 1 })
db.brick.createIndex({
    "text": "text",
    "attributes.text": "text",
    "attributes.values.text": "text",
},{
    weights: {
        "text": 10,
        "attributes.text": 5,
        "attributes.values.text": 1
    },
    name: "textIdx"
})
```

### Atlas FTS Definition

```javascript
{
    "analyzer": "lucene.standard",
    "searchAnalyzer": "lucene.standard",
    "mappings": {
        "dynamic": false,
        "fields": {
            "attributes": {
                "type": "document",
                "fields": {
                    "values": {
                        "type": "document",
                        "fields": {
                            "text": {
                                "type": "string"
                            }
                        }
                    },
                    "text": {
                        "type": "string"
                    }
                }
            },
            "text": {
                "type": "string",
                "analyzer": "lucene.standard"
            }
        }
    }
}
```

## Queries

### Find All Bricks for Family

```javascript
db.brick.find({
    "familyCode": 50200000
})
```

### Find All Bricks for Class

```javascript
db.brick.find({
    "classCode": 50202200
})
```

### Find All Bricks for Segment

```javascript
db.brick.find({
    "segmentCode": 50000000
})
```

### Find Brick for Id

```javascript
db.brick.find({
    "_id": 10001688
})
```

### Find All Attributes for Brick

```javascript
db.brick.find({
    "_id": 10001688
},{
    "attributes": 1
})
```

### Find All Bricks with Attributes x…

```javascript
db.brick.find({
    "attributes.code": 20003029
})
```

### Find all Bricks with Attributes x… and of Class y

```javascript
db.brick.find({
    "classCode": 94022200,
    "attributes.code": 20003029
})
```

### Count all Bricks for Class

```javascript
db.brick.count({
    "classCode": 94022200
})
```

### Count all Bricks for Family

```javascript
db.brick.count({
    "familyCode": 50200000
})
```

### Find all Bricks from date created

```javascript
db.brick.find({
    "creationDate": { $gt: ISODate("2020-10-19T11:16:15.309Z") }
})
```

### Find all Classes

Use *$group* to extract the distinct classes from bricks or create a dynamic or materialised view.

Probably just create separate collections for segments, families and classes which contains the full metadata and only embed relevant information into bricks for fast querying.

```javascript
// dynamic view
db.createView("classes", "brick", [
    {
        $group: {
            _id: "$classCode",
            classText: { $first: "$classText" }
        }
    },{
        $project: {
            _id: 0,
            classCode: "$_id",
            classText: 1
        }
    }
])

// materialized view
db.brick.aggregate([
    {
        $group: {
            _id: "$classCode",
            classText: { $first: "$classText" }
        }
    },{
        $out: "classes"
    }
])

// search
db.classes.find()
```

### Find All Families

Could use *$group* to extract the distinct families from bricks or create a dynamic or materialised view as below.

Probably just create separate collections for segments, families and classes which contains the full metadata and only embed relevant information into bricks for fast querying.

```javascript
// dynamic view
db.createView("families", "brick", [
    {
        $group: {
            _id: "$familyCode",
            familyText: { $first: "$familyText" }
        }
    },{
        $project: {
            _id: 0,
            familyCode: "$_id",
            familyText: 1
        }
    }
])

// materialised view
db.brick.aggregate([
    {
        $group: {
            _id: "$familyCode",
            familyText: { $first: "$familyText" }
        }
    }, {
        $out: "families"
    }
])

// search
db.families.find()
```

### Full text search of brick by name and attribute

MongoDB *$text* index can used for this. Dields can be weighted and results can be sorted by relevance. Stemming and pluralisation is performed but does not support partial word matching.

Atlas full-text search goes further; it supports partial word matches, autocomplete capabilities and a dizzying array of other options.
It is backed by the Lucene engine (used by Elastic amongst other products). Atlas automatically keeps the Lucene index up-to-date as data is inserted / modified - no user intervention required.

```javascript
// full text search using MongoDB text index
db.brick.find(
    {
        $text: { $search: "cigars" }
    },
    {
      text: 1,
      "attributes.text": 1,
        score: { $meta: "textScore" }
    }
).sort({ score: { $meta: "textScore" } })

// fuzzy full text search using Atlas
db.brick.aggregate([{
    $search: {
      "text": {
        "query": "Cigars",
        "path": ["text", "attributes.text", "attributes.values.text"],
        "fuzzy": {
          maxEdits: 2
        }
      }
    }
  },
  {
    $project: {
      text: 1,
      "attributes.text": 1,
      score: { $meta: "searchScore" }
    }
}])
```

## Data Changes

### Updating Class text

This would be wrapped in a transaction to make the update atomic.

```javascript
db.brick.updateMany({
    classCode: 50270100
},{
    $set: {
        classText: "New Fruit name"
    }
})
```

### Adding a new Attribute to a Brick

```javascript
db.brick.updateOne({
    _id: 10000002  // brickCode
},{
    $push: {
        attributes: {
            code: 20000999,
            text: "If Tropical",
            values: [{
                code: 30002960,
                text: "NO"
            },{
                code: 30002518,
                text: "UNIDENTIFIED"
            },{
                code: 30002654,
                text: "YES"
            }]
        }
    }
})
```

### Adding a new Value to an Attribute

This would be wrapped in a transaction to make the update atomic.

```javascript
db.brick.updateMany({
    "attributes.code": 20003029
},{
    $push: {
        "attributes.$[attr].values": {
            code: 30009999,
            text: "DISPLAY"
        }
    }
},{
    arrayFilters: [{
        "attr.code": 20003029
    }]
})
```

### Renaming an existing Attribute text

Change value text for 30002518 from "UNIDENTIFIED" to "UNKNOWN".

Wrap with transaction for atomicity.

```javascript
db.brick.updateMany({
    "attributes.values.code": 30002518
},{
    $set: {
        "attributes.$[].values.$[val].text": "UNKNOWN"
    }
},{
    arrayFilters: [{
        "val.code": 30002518
    }]
})
```

## Benchmark

### Setup

* M10 Atlas Cluster (2x vCPU, 2GB RAM)
* 8-Core AWS EC2 in the same Region as Atlas Cluster

### Search for a single brick

`{ _id: 10007523 }`

| time                | queries/s | results/s | latency p50 | latency p95 | latency p99 | progress |
|---------------------|-----------|-----------|-------------|-------------|-------------|----------|
| 2020-10-19T15:46:15 | 1937      | 1937      | 1.31ms      | 2.35ms      | 7.38ms      | 1%       |
| 2020-10-19T15:46:16 | 7026      | 7026      | 975us       | 1.55ms      | 3.61ms      | 8%       |
| 2020-10-19T15:46:17 | 8167      | 8167      | 911us       | 1.06ms      | 1.32ms      | 16%      |
| 2020-10-19T15:46:18 | 8959      | 8959      | 845us       | 1.01ms      | 1.34ms      | 25%      |
| 2020-10-19T15:46:19 | 9113      | 9113      | 838us       | 987us       | 1.25ms      | 34%      |
| 2020-10-19T15:46:20 | 9025      | 9025      | 839us       | 979us       | 1.22ms      | 43%      |
| 2020-10-19T15:46:21 | 9120      | 9120      | 837us       | 980us       | 1.18ms      | 52%      |
| 2020-10-19T15:46:22 | 9130      | 9130      | 839us       | 970us       | 1.11ms      | 61%      |
| 2020-10-19T15:46:23 | 8899      | 8899      | 838us       | 1.01ms      | 1.28ms      | 70%      |
| 2020-10-19T15:46:24 | 9094      | 9094      | 837us       | 979us       | 1.26ms      | 79%      |
| 2020-10-19T15:46:25 | 9093      | 9093      | 840us       | 968us       | 1.12ms      | 88%      |
| 2020-10-19T15:46:26 | 8595      | 8595      | 843us       | 1.09ms      | 2.10ms      | 97%      |
| 2020-10-19T15:46:27 | 8611      | 8611      | 845us       | 1.04ms      | 1.42ms      | 100%     |

***MongoDB CPU @ ~70%***

### Search for all bricks with attribute (~822 bricks)

`{ "attributes.code": 20003029 }`

#### Initial run

| time                | queries/s | docs/s | latency p50 | latency p95 | latency p99 |
|---------------------|-----------|--------|-------------|-------------|-------------|
| 2020-10-19T16:00:07 | 160       | 131931 | 50.0ms      | 570ms       | 620ms       |
| 2020-10-19T16:00:08 | 206       | 169042 | 38.5ms      | 75.0ms      | 105ms       |
| 2020-10-19T16:00:09 | 200       | 164349 | 38.4ms      | 69.3ms      | 94.6ms      |
| 2020-10-19T16:00:10 | 201       | 165312 | 39.0ms      | 74.1ms      | 88.5ms      |
| 2020-10-19T16:00:11 | 204       | 167299 | 37.2ms      | 77.0ms      | 88.6ms      |
| 2020-10-19T16:00:12 | 199       | 163720 | 38.9ms      | 78.6ms      | 88.5ms      |
| 2020-10-19T16:00:13 | 201       | 164830 | 39.1ms      | 77.3ms      | 87.9ms      |
| 2020-10-19T16:00:14 | 201       | 165533 | 38.9ms      | 76.8ms      | 88.3ms      |
| 2020-10-19T16:00:15 | 206       | 169149 | 38.2ms      | 69.2ms      | 79.4ms      |
| 2020-10-19T16:00:16 | 304       | 249608 | 39.3ms      | 69.6ms      | 98.7ms      |

***MongoDB CPU @ 100% causing high latencies and queuing***

#### Second run - TPS limited to 100 in the test harness

| time                | operations/s | results/s | latency p50 | latency p95 | latency p99 |
|---------------------|--------------|-----------|-------------|-------------|-------------|
| 2020-10-19T16:05:51 | 48           | 39571     | 8.47ms      | 549ms       | 588ms       |
| 2020-10-19T16:05:52 | 100          | 82564     | 7.93ms      | 21.4ms      | 25.0ms      |
| 2020-10-19T16:05:53 | 98           | 80733     | 7.58ms      | 11.1ms      | 11.6ms      |
| 2020-10-19T16:05:54 | 98           | 80611     | 7.43ms      | 11.3ms      | 11.7ms      |
| 2020-10-19T16:05:55 | 99           | 81425     | 7.38ms      | 8.02ms      | 19.5ms      |
| 2020-10-19T16:05:56 | 98           | 80611     | 7.46ms      | 7.90ms      | 10.9ms      |
| 2020-10-19T16:05:57 | 99           | 81423     | 7.34ms      | 7.88ms      | 11.2ms      |
| 2020-10-19T16:05:58 | 99           | 81444     | 7.29ms      | 7.69ms      | 10.9ms      |
| 2020-10-19T16:05:59 | 99           | 81422     | 7.23ms      | 7.59ms      | 11.0ms      |
| 2020-10-19T16:06:00 | 98           | 80599     | 7.32ms      | 8.76ms      | 13.9ms      |
| 2020-10-19T16:06:01 | 99           | 81510     | 7.28ms      | 8.34ms      | 10.9ms      |
| 2020-10-19T16:06:02 | 99           | 81450     | 8.10ms      | 10.8ms      | 18.1ms      |

***MongoDB CPU @ 35% - latencies more reasonable***

### Search across brick text, attribute text and attribute value text using FTS

`{ $text: { $search: "cigars" } }`

| time                | queries/s | docs/s | latency p50 | latency p95 | latency p99 |
|---------------------|-----------|--------|-------------|-------------|-------------|
| 2020-10-19T16:21:16 | 1723      | 5169   | 1.36ms      | 3.39ms      | 11.6ms      |
| 2020-10-19T16:21:17 | 5609      | 16827  | 1.07ms      | 3.16ms      | 6.45ms      |
| 2020-10-19T16:21:18 | 7383      | 22150  | 993us       | 1.25ms      | 1.61ms      |
| 2020-10-19T16:21:19 | 7595      | 22786  | 967us       | 1.31ms      | 1.79ms      |
| 2020-10-19T16:21:20 | 7815      | 23444  | 950us       | 1.32ms      | 1.73ms      |
| 2020-10-19T16:21:21 | 7739      | 23217  | 952us       | 1.35ms      | 1.82ms      |
| 2020-10-19T16:21:22 | 7328      | 21983  | 960us       | 1.45ms      | 2.76ms      |
| 2020-10-19T16:21:23 | 7622      | 22866  | 954us       | 1.30ms      | 2.25ms      |
| 2020-10-19T16:21:24 | 7882      | 23646  | 946us       | 1.27ms      | 1.60ms      |
| 2020-10-19T16:21:25 | 7778      | 23335  | 942us       | 1.26ms      | 1.63ms      |

***MongoDB CPU @ ~80%***