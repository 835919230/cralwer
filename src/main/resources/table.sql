DROP DATABASE IF EXISTS `crawler`;
CREATE DATABASE `crawler`;
USE crawler;

DROP TABLE IF EXISTS `FlightInfo`;
CREATE TABLE `FlightInfo`
(
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '主键',
  `depCity` VARCHAR(40) COMMENT '起点',
  `arrCity` VARCHAR(40) COMMENT '终点',
  `depDate` VARCHAR(40) COMMENT '日期',
  `flightNo` VARCHAR(40) COMMENT '航班号，用逗号分割',
  `depTime` VARCHAR(40) COMMENT '起飞时间',
  `price` DOUBLE COMMENT '价格',
  `seat` INT COMMENT '当前最大座位数',
  `createTime` TIMESTAMP COMMENT '入库时间',
  UNIQUE KEY(`flightNo`, `depDate`)
)AUTO_INCREMENT=1000;

DROP TABLE IF EXISTS `FlightUser`;
CREATE TABLE FlightUser
(
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '主键',
  `name` VARCHAR(40) NOT NULL COMMENT '乘客姓名',
  `cardNo` VARCHAR(60) NOT NULL COMMENT '证件号码',
  `phone` VARCHAR(40) NOT NULL COMMENT '联系方式'
)AUTO_INCREMENT=100;

# CREATE TABLE FlightOrder