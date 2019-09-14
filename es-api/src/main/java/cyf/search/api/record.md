#rest API

1. 获取所有index
    ```
    curl -XGET localhost:9200/_cat/indices
    ```
2. 查看mapping

    a.所有index的mapping【pretty参数是json格式化输出】
    ```
    curl -XGET localhost:9200/_mapping?pretty
    ```
     b.获取某一index
     ```
        curl -XGET localhost:9200/xxx/_mapping?pretty
   ```
   
3.   创建index
        ```
        curl -XPUT 'localhost:9200/customer
     ```
4. 保存单个数据：
    ```
        curl -XPUT 'localhost:9200/customer/external/1?pretty&pretty' -d'
            {
              "name": "John Doe"
            }'
    ``` 
 5. 批量：
     ```
        curl -XPOST 'localhost:9200/customer/external/_bulk?prett
    ```
      
6. 查询：【注意单引号】
    ```
    curl -XGET 'localhost:9200/bank/_search?pretty' -d'
    {
       "from":0,
        "size":1,
      "query": { "match_all": {} },
      "sort": [
        { "account_number": "asc" }
      ]
    }
   ```     
