## RandomTable
随机表

#### 特性
低内存占用：动态的生成和步进器的原理使得内存开销并不是很大
快速生成：基于矩阵和偏移量的取值
唯一性：必然生成且只生成一次的范围内的数字
平均分布：生成的数字是平均分布的
种子支持：支持从种子建立随机表
  
#### API
实例化  
@pamar start: 起始  
@pamar stop: 结束  
@pamar seed: 种子  
``` new Random ( int start, int stop, ?long seed ) ;```

获取下一个随机值  
``` Ramdon.next( ) ```
