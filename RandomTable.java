package net.atcat.pigeonengine.util;

import java.util.Random;

public class RandomTable {

    protected final long seed ; // 种子
    protected final boolean haveSeed ; // 拥有种子

    protected final int width ; // 表宽
    protected final int height ; // 表高
    protected final int start ; // 起始
    protected final int overflowStart ; // 溢出起始

    protected int next ; // 下一位数字

    protected int dataCount ; // 数据表计数器
    protected int readCount ; // 偏移表计数器
    protected int overflowCount ; // 溢出表计数器

    protected final int[] readTable ; // 读取顺序表
    protected final int[] dataTable ; // 数据表
    protected final int[] preTable ; // 预读表
    protected final int[] overflowTable ; // 溢出表
    protected final int[] offsetTable ; // 偏移表

    protected Random rdm = new Random( ) ; // 随机

    /** 随机表
     *
     * @param start   起始
     * @param end     结束
     *
     * @apiNote 生成的整数区间是 [start, end]
     */
    public RandomTable( int start, int end ) {
        this( start, end, 0, false ) ;
    } ;
    /** 随机表
     *
     * @param start   起始
     * @param end     结束
     * @param seed    种子
     *
     * @apiNote 生成的整数区间是 [start, end]
     */
    public RandomTable( int start, int end, long seed ) {
        this( start, end, seed, true ) ;
    } ;
    /** 内部随机表
     *
     * @param start      起始
     * @param end        结束
     * @param seed       种子
     * @param haveSeed   是否拥有种子
     *
     * @apiNote 生成的整数区间是 [start, end]
     */
    protected RandomTable( int start, int end, long seed, boolean haveSeed ) {

        // 尺寸计算
        int size = Math.abs( start - end ) +1;

        // 赋值
        this.seed = seed ;
        this.haveSeed = haveSeed ;
        this.start = start ;
        this.height = (int) Math.sqrt( size ) ; // 高
        this.width = size / this.height ; // 设置平方根
        this.overflowStart = this.height * this.width ; // 起始值

        // 初始化表
        this.dataTable = new int[this.height] ;
        this.preTable = new int[this.height] ;
        this.offsetTable = new int[this.height] ;
        // 初始化溢出表
        this.overflowTable = new int[ size - this.overflowStart ] ;
        // 初始化读取表
        this.readTable = new int[this.width] ;

        // 重置
        this.reset( ) ;

    } ;
    // 混合
    protected static void combination ( int[] arr1, int[] arr2, Random random ) {
        int cache ;
        for ( int i = 0 ; i < arr1.length ; i++ ) {
            if ( random.nextBoolean( ) ) {
                cache = arr1[i] ;
                arr1[i] = arr2[i] ;
                arr2[i] = cache ;
            } ;
        } ;
    } ;
    // 打乱
    protected static void shuffle( int[] arr, Random random ) {
        int i = arr.length ;
        int cache ;
        while ( i > 0 ) {
            int n = random.nextInt( i-- ) ; // 取得随机值
            cache = arr[i] ; // 地址
            arr[i] = arr[n] ; // 交换
            arr[n] = cache ; // 还原
        } ;
    } ;
    // 获得矩阵中的值
    protected static int getMatrixNum( int width, int height, int offset ) {
        return width * height + getLoopNum( offset +height, width ) ;
    } ;
    // 获得一个循环值
    protected static int getLoopNum( int base, int max ) {
        int ret = base ;
        while ( ret >= max )
            ret -= max ;
        return ret ;
    } ;
    // 重置
    protected void reset( ) {
        // 重置随机数
        this.rdm = this.haveSeed ? new Random( this.seed ) : new Random( ) ;
        // 重置计数器
        this.overflowCount = 0 ;
        this.readCount = 0 ;
        this.dataCount = 0 ;
        // 重设并打乱读取顺序表
        for ( int i = 0; i < this.readTable.length ; i++ )
            this.readTable[i] = i ;
        shuffle( this.readTable, this.rdm ) ;
        // 重设并打乱溢出表
        for ( int i = 0; i < this.overflowTable.length ; i++ ) {
            this.overflowTable[i] = this.overflowStart + i ;
        } ;
        shuffle( this.overflowTable, this.rdm ) ;
        // 重设偏移表
        for ( int i = 0; i < this.offsetTable.length ; i++ ) {
            this.offsetTable[i] = this.rdm.nextInt( this.readTable.length ) ;
        } ;
        // 刷新表
        this.flushTable( ) ;
    } ;
    // 刷新表
    protected void flushTable( ) {
        // 已无法生成数据和预览
        if ( this.readCount > this.readTable.length ) {
            // 重设
            this.reset( ) ;
        } else {
            // 计数器是新的
            if ( this.readCount == 0 ) {
                // 生成数据到数据表
                for ( int i = 0; i < this.dataTable.length ; i++ ) {
                    // 取得当前应该读取的列加上当前行的偏移量
                    this.dataTable[i] = getMatrixNum( this.width, i, this.readTable[ this.readCount ] +this.offsetTable[i] ) ;
                } ;
                this.readCount ++ ;
            } else {
                // 拷贝预览表的值到数据表
                System.arraycopy( this.preTable, 0, this.dataTable, 0, this.height ) ;
            } ;
            // 计数器未用完
            if ( this.readCount < this.readTable.length ) {
                // 生成数据到预览表
                for ( int i = 0; i < this.dataTable.length ; i++ ) {
                    this.preTable[i] = getMatrixNum( this.width, i, this.readTable[ this.readCount ] +this.offsetTable[i] ) ;
                } ;
                // 混合表
                combination( this.preTable, this.dataTable, this.rdm ) ;
            } ;
            // 增加计数器
            this.readCount ++ ;
            // 重设数据表值
            this.dataCount = 0 ;
            // 随机化数据表
            shuffle( this.dataTable, this.rdm ) ;
            // 存在溢出
            if ( this.overflowCount < this.overflowTable.length ) {
                // 将溢出的第一位和表的随机一位进行交换
                int index = this.rdm.nextInt( this.overflowTable.length ) ;
                this.next = this.dataTable[ index ] ;
                this.dataTable[ index ] = this.overflowTable[ this.overflowCount++ ] ;
            } else {
                // 使用表值
                this.next = this.dataTable[ this.dataCount++ ] ;
            } ;
        }
    } ;
    // 获取下一位值
    protected int flushNextInt( ) {
        int ret = this.next;
        if ( this.dataCount < this.dataTable.length ) {
            // 未读取完时进行下一位
            this.next = this.dataTable[ this.dataCount++ ] ;
        } else {
            // 读取完时刷新
            this.flushTable( ) ;
        } ;
        return ret ;
    } ;
    // 对外暴露
    public int next( ) {
        return this.flushNextInt( ) + this.start ;
    } ;

}
