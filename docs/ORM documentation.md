##Text Based Query

Domain Specific Query Language (DS-QL)

```
Station(name).limit(0, 50)/
  category.where(code=$group)/../
  series(^name:station,name,category).where(code in ("T_MIN","T_MAX"))/
    unit(name:unit)/../
  observations(time.timestamp()).where(time>="2015-04-12" and time<"2014-04-13")/
    value>NumericValue(value.round(1):v)
```

```
SELECT
  FROM station.group()
  INNER JOIN series.group()
  INNER JOIN observation(value.avg, value.min, value.max, value.count, value.sum).group(time)

  GROUP BY station.id, series.id
```
