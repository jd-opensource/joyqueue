# Performance benchmark
  JournalQ is a distributed pub/sub messaging system,with highly scalable and low latency.you maybe interested in the performance of JounalQ if you prepare to deploy and use it in production. In this section,a simple benchmark is designed to show JournalQ's produce performance on single instance.


## Set up
For the tests,hareware requirement show as following:

* Intel(R) Xeon(R) CPU E5-2620 v4 @ 2.10GHz with 8 cores * 2
* 32GB x 8 RAM
* 960GB x 6 SSD  
* 10 Gigabit Ethernet


## Test case

A topic with 200 partitions was created,using compression lz4 algorithm if required,per message size 1024 byte.Actually,the performance of producer was affected by whether synchronous or asynchronous message you choose, asynchronous message won't blocking the producer, so it allows the producer to continuely produce messages and process result as they want. In addition, message batch size and compression for large message body are important ways to improve the producer performance. we carefully test 4 cases show below:

* Case 1: sync produce, 1 message
* Case 2: sync produce, 1 message with compression
* Case 3: async produce,100 messages batch with compression
* Case 4: async produce,100 messages batch with compression,and 60 consumers as the same time  


## Result and Conclusion
Experiment run each benchmark case duration 10 minutes,and the result show in the following table.

| case | CPU(%) |Mem(%)| Network I/O(MB/s) | Disk I(MB/s)|QPS|
| :----:| :----: |:----:|:----:|:----:|:----:|
|1 | 75 |12.22 |363.48/37.82|360.37 |309763|  
|2 | 76 |12.22 |360.67/37.07|353.79 |308037|   
|3 | 19 |11.88 |886.11/4.63 |1376.31|32961776|
|4 | 46 |10.1|686.44/1172.51|1034.93|25018477|

The table above shows that compression for single message has little impacts on the producer performance,but will significantly improve the performance when we have a large batch messages,and the large batch messages usually benifit for cpu and disk i/o. JorunalQ can keep high throughputs either on single message or large batch messages scene.

| Left Aligned | Centered | Right Aligned | Left Aligned | Centered | Right Aligned |
| :----------- | :------: | ------------: | :----------- | :------: | ------------: |
| Cell 1       | Cell 2   | Cell 3        | Cell 4       | Cell 5   | Cell 6        |
| Cell 7       | Cell 8   | Cell 9        | Cell 10      | Cell 11  | Cell 12       |
