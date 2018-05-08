-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: measurehistory
-- ------------------------------------------------------
-- Server version	5.7.18-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `plc`
--

DROP TABLE IF EXISTS `plc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plc` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET utf8mb4 NOT NULL,
  `ip` varchar(45) CHARACTER SET utf8mb4 NOT NULL,
  `rack` int(11) NOT NULL DEFAULT '0' COMMENT 'Declare the PLC Rack number (to find in HW-Config)',
  `slot` int(11) NOT NULL DEFAULT '0' COMMENT 'Declare the PLC Slot number (to find in HW-Config)',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT 'set type of PLC / readonly when PLC is connected:\n0: S7_300_400_compatibel; 1: S7_200_compatibel;\n2: S7_1200_compatibel; 3: S7_1500_compatibel;\n4: WinAC_RTX_2010_compatibel; 5: Logo_compatibel;\n6: Logo0BA7_compatibel; 7: Logo0BA8_compatibel;',
  `timeout` int(11) NOT NULL DEFAULT '5000' COMMENT 'Declare the connect timeout in milliseconds',
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `plc$ip_UNIQUE` (`ip`),
  UNIQUE KEY `plc$name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plc`
--

LOCK TABLES `plc` WRITE;
/*!40000 ALTER TABLE `plc` DISABLE KEYS */;
/*!40000 ALTER TABLE `plc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plctrigger`
--

DROP TABLE IF EXISTS `plctrigger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plctrigger` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `db` int(11) NOT NULL,
  `strt_adr` int(11) NOT NULL,
  `activated` tinyint(1) NOT NULL DEFAULT '0',
  `plc_id` int(11) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_plc_idx` (`plc_id`),
  CONSTRAINT `fk_plc` FOREIGN KEY (`plc_id`) REFERENCES `plc` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plctrigger`
--

LOCK TABLES `plctrigger` WRITE;
/*!40000 ALTER TABLE `plctrigger` DISABLE KEYS */;
/*!40000 ALTER TABLE `plctrigger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `process`
--

DROP TABLE IF EXISTS `process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `station` varchar(45) CHARACTER SET utf8mb4 NOT NULL,
  `name` varchar(45) CHARACTER SET utf8mb4 NOT NULL,
  `setvalue_used` tinyint(1) NOT NULL DEFAULT '0',
  `setvalue` float NOT NULL DEFAULT '0',
  `decpoints` int(11) NOT NULL DEFAULT '0',
  `nr_avg` int(11) NOT NULL DEFAULT '0',
  `nr_spc_class` int(11) NOT NULL DEFAULT '10',
  `cpk_lolim1` float NOT NULL DEFAULT '1.33',
  `cpk_lolim2` float NOT NULL DEFAULT '1.66',
  `unit_id` int(11) NOT NULL,
  `plctrigger_id` int(11) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `process$plctrigger_id_UNIQUE` (`plctrigger_id`),
  KEY `fk_measure_unit1_idx` (`unit_id`),
  KEY `fk_process_plctrigger1_idx` (`plctrigger_id`),
  CONSTRAINT `fk_measure_unit` FOREIGN KEY (`unit_id`) REFERENCES `unit` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_process_plctrigger` FOREIGN KEY (`plctrigger_id`) REFERENCES `plctrigger` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `process`
--

LOCK TABLES `process` WRITE;
/*!40000 ALTER TABLE `process` DISABLE KEYS */;
/*!40000 ALTER TABLE `process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `result`
--

DROP TABLE IF EXISTS `result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `result` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `serial` varchar(20) CHARACTER SET utf8mb4 DEFAULT NULL,
  `typ_nr` int(11) DEFAULT NULL,
  `value` float DEFAULT NULL,
  `lolim` float DEFAULT NULL,
  `uplim` float DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `wt_nr` varchar(20) CHARACTER SET utf8mb4 DEFAULT NULL,
  `remark` varchar(50) CHARACTER SET utf8mb4 DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `process_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_result_process1_idx` (`process_id`),
  CONSTRAINT `fk_result_process` FOREIGN KEY (`process_id`) REFERENCES `process` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `result`
--

LOCK TABLES `result` WRITE;
/*!40000 ALTER TABLE `result` DISABLE KEYS */;
/*!40000 ALTER TABLE `result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shift1_strt_time` time(6) NOT NULL DEFAULT '06:00:00.000000' COMMENT 'Schicht 1 Startzeit',
  `shift1_end_time` time(6) NOT NULL DEFAULT '14:00:00.000000' COMMENT 'Schicht 2 Startzeit',
  `shift2_strt_time` time(6) NOT NULL DEFAULT '14:00:00.000000' COMMENT 'Schicht 2 Startzeit',
  `shift2_end_time` time(6) NOT NULL DEFAULT '22:00:00.000000' COMMENT 'Schicht 2 Startzeit',
  `shift3_strt_time` time(6) NOT NULL DEFAULT '22:00:00.000000' COMMENT 'Schicht 3 Startzeit',
  `shift3_end_time` time(6) NOT NULL DEFAULT '06:00:00.000000' COMMENT 'Schicht 3 Startzeit',
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES (1,'06:00:00.000000','14:00:00.000000','14:00:00.000000','22:00:00.000000','22:00:00.000000','06:00:00.000000','2017-09-06 21:21:20');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unit`
--

DROP TABLE IF EXISTS `unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sign` varchar(10) CHARACTER SET utf8mb4 NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unit$sign_UNIQUE` (`sign`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unit`
--

LOCK TABLES `unit` WRITE;
/*!40000 ALTER TABLE `unit` DISABLE KEYS */;
INSERT INTO `unit` VALUES (1,'mm','2017-08-25 06:50:17'),(2,'ml','2017-03-08 10:01:58'),(3,'Ncm','2017-03-08 10:01:59'),(4,'g','2017-03-08 10:01:59'),(5,'cm³/min','2017-03-08 10:01:59'),(6,'mbar','2017-03-08 10:01:59'),(7,'Nm','2017-03-08 10:01:59'),(8,'mA','2017-03-08 10:01:59'),(9,'Grad','2017-03-08 10:01:59'),(10,'°C','2017-03-08 10:01:59'),(11,'V','2017-03-08 10:01:59'),(12,'bar','2017-03-08 10:01:59'),(13,'kg','2017-03-08 10:01:59'),(14,'l','2017-03-08 10:01:59'),(15,'','2017-03-08 10:01:59'),(16,'N','2017-08-03 11:59:23'),(17,'kN','2017-09-06 21:22:56'),(18,'mV','2017-09-06 21:22:56'),(19,'kV','2017-09-06 21:22:56'),(20,'kA','2017-09-06 21:22:56'),(21,'s','2017-09-06 21:26:36'),(22,'min','2017-09-06 21:26:36'),(23,'h','2017-09-06 21:26:36'),(24,'kW','2017-09-06 21:27:20'),(25,'W','2017-09-06 21:27:20'),(26,'Wh','2017-09-06 21:27:20'),(27,'kWh','2017-09-06 21:27:20');
/*!40000 ALTER TABLE `unit` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-09-13 15:32:49
