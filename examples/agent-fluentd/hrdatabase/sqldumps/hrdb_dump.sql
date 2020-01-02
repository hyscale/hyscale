-- MySQL dump 10.16  Distrib 10.2.8-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: hrdb
-- ------------------------------------------------------
-- Server version	10.2.8-MariaDB-10.2.8+maria~trusty

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
-- Current Database: `hrdb`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `hrdb` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;

USE `hrdb`;

--
-- Table structure for table `DEPARTMENT`
--

DROP TABLE IF EXISTS `DEPARTMENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DEPARTMENT` (
  `DEPTID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BUDGET` int(11) DEFAULT NULL,
  `Q1` int(11) DEFAULT NULL,
  `Q2` int(11) DEFAULT NULL,
  `Q3` int(11) DEFAULT NULL,
  `Q4` int(11) DEFAULT NULL,
  `DEPTCODE` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LOCATION` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TENANTID` int(11) DEFAULT NULL,
  PRIMARY KEY (`DEPTID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DEPARTMENT`
--

LOCK TABLES `DEPARTMENT` WRITE;
/*!40000 ALTER TABLE `DEPARTMENT` DISABLE KEYS */;
INSERT INTO `DEPARTMENT` VALUES (1,'Engineering',1936760,445455,522925,426087,542293,'Eng','San Francisco',1),(2,'Marketing',1129777,225955,271146,327635,305040,'Mktg','New York',1),(3,'General and Admin',1452570,435771,290514,348617,377668,'G&A','San Francisco',1);
/*!40000 ALTER TABLE `DEPARTMENT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EMPLOYEE`
--

DROP TABLE IF EXISTS `EMPLOYEE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EMPLOYEE` (
  `EID` int(11) NOT NULL AUTO_INCREMENT,
  `FIRSTNAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LASTNAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STREET` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CITY` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STATE` varchar(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ZIP` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BIRTHDATE` date DEFAULT NULL,
  `PICURL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `JOBTITLE` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DEPTID` int(11) DEFAULT NULL,
  `MANAGERID` int(11) DEFAULT NULL,
  `TENANTID` int(11) DEFAULT NULL,
  PRIMARY KEY (`EID`),
  KEY `DEPTFKEY` (`DEPTID`),
  KEY `MGRFKEY` (`MANAGERID`),
  CONSTRAINT `DEPTFKEY` FOREIGN KEY (`DEPTID`) REFERENCES `DEPARTMENT` (`DEPTID`),
  CONSTRAINT `MGRFKEY` FOREIGN KEY (`MANAGERID`) REFERENCES `EMPLOYEE` (`EID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EMPLOYEE`
--

LOCK TABLES `EMPLOYEE` WRITE;
/*!40000 ALTER TABLE `EMPLOYEE` DISABLE KEYS */;
INSERT INTO `EMPLOYEE` VALUES (1,'Eric','Lin','45 Houston Street','New York','NY','10106','1973-10-21','https://s3.amazonaws.com/wmstudio-apps/salesrep/Eric-Lin.png','Product Manager',1,NULL,1),(2,'Brad','Tucker','25 Liberty PI','Boston','MA','02127','1991-03-19','https://s3.amazonaws.com/wmstudio-apps/salesrep/Brad-Tucker.png','Engineer',1,1,1),(3,'Chris','Madison','2525 Cypress Lane','Atlanta','GA','14231','1975-09-30','https://s3.amazonaws.com/wmstudio-apps/salesrep/Chris-Madison.png','Architect',1,1,1),(5,'Jane','Lisa','346 Mulholland Drive','Los Angeles','CA','94036','1984-02-25','https://s3.amazonaws.com/wmstudio-apps/salesrep/Jane-Lisa.png','Marketing Lead',2,NULL,1),(6,'Jessica','Bennet','6000 Sunset Boulevard','Los Angeles','CA','90028','1992-09-30','https://s3.amazonaws.com/wmstudio-apps/salesrep/Jessica-Bennet.png','Marketing Executive',2,5,1),(7,'Keith','Neilson','3 YukYuk Way','San Gabriel','CA','91775','1988-03-04','https://s3.amazonaws.com/wmstudio-apps/salesrep/Keith-Neilson.png','Admin Assistant',3,NULL,1);
/*!40000 ALTER TABLE `EMPLOYEE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER`
--

DROP TABLE IF EXISTS `USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER` (
  `USERID` int(11) NOT NULL AUTO_INCREMENT,
  `USERNAME` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PASSWORD` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ROLE` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TENANTID` int(11) DEFAULT NULL,
  PRIMARY KEY (`USERID`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER`
--

LOCK TABLES `USER` WRITE;
/*!40000 ALTER TABLE `USER` DISABLE KEYS */;
INSERT INTO `USER` VALUES (1,'admin','admin','adminrole',1),(2,'user','user','userrole',1),(3,'admin2','admin2','adminrole',2),(4,'user2','user2','userrole',2),(6,'anon','anon','anonymous',1);
/*!40000 ALTER TABLE `USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `VACATION`
--

DROP TABLE IF EXISTS `VACATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `VACATION` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `STARTDATE` date DEFAULT NULL,
  `ENDDATE` date DEFAULT NULL,
  `EMPID` int(11) NOT NULL,
  `TENANTID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `EMPFKEY` (`EMPID`),
  CONSTRAINT `EMPFKEY` FOREIGN KEY (`EMPID`) REFERENCES `EMPLOYEE` (`EID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `VACATION`
--

LOCK TABLES `VACATION` WRITE;
/*!40000 ALTER TABLE `VACATION` DISABLE KEYS */;
INSERT INTO `VACATION` VALUES (1,'2014-11-23','2014-11-27',1,1),(2,'2014-12-05','2014-12-07',1,1),(3,'2014-10-06','2014-11-12',2,1),(4,'2014-01-01','2014-02-16',2,1),(5,'2014-08-14','2014-11-16',3,1);
/*!40000 ALTER TABLE `VACATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'hrdb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-11-23 11:53:54
