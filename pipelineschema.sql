CREATE DATABASE  IF NOT EXISTS `camundabpmajsc6` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `camundabpmajsc6`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Database: camundabpmajsc6
-- ------------------------------------------------------
-- Server version	5.5.5-10.1.17-MariaDB

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
-- Table structure for table `application_log`
--

DROP TABLE IF EXISTS `application_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application_log` (
  `application_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(45) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `pipeline_id` varchar(45) DEFAULT NULL,
  `pipeline_name` varchar(45) DEFAULT NULL,
  `submodule_name` varchar(45) DEFAULT NULL,
  `process_name` varchar(45) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `class_name` varchar(45) DEFAULT NULL,
  `method_name` varchar(45) DEFAULT NULL,
  `log_data` blob,
  `user_friendly` bit(1) DEFAULT NULL,
  PRIMARY KEY (`application_log_id`),
  KEY `Pipeline_id_index` (`pipeline_id`),
  KEY `Class_name_method_name_index` (`class_name`,`method_name`),
  KEY `Name` (`name`),
  KEY `time` (`time`),
  KEY `gots_id` (`gots_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4405152 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `asf_gots`
--

DROP TABLE IF EXISTS `asf_gots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `asf_gots` (
  `gots_id` varchar(45) NOT NULL,
  `Acronym` varchar(145) DEFAULT NULL,
  `Application_name` varchar(450) DEFAULT NULL,
  `Description` blob,
  `Appl_Contact` varchar(145) DEFAULT NULL,
  `Application_Type` varchar(145) DEFAULT NULL,
  `IT_LTM` varchar(145) DEFAULT NULL,
  `PCI_Data` varchar(45) DEFAULT NULL,
  `Internet_Facing_Indicator` varchar(45) DEFAULT NULL,
  `created_date` varchar(45) DEFAULT NULL,
  `namespace` varchar(450) DEFAULT NULL,
  `onboarded` bit(1) DEFAULT NULL,
  `associated_gots_id` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`gots_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `build_server_environments`
--

DROP TABLE IF EXISTS `build_server_environments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `build_server_environments` (
  `build_server_environment_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(45) DEFAULT NULL,
  `environment_name` varchar(450) DEFAULT NULL,
  `environment_url` varchar(450) DEFAULT NULL,
  `username` varchar(450) DEFAULT NULL,
  `password` varchar(450) DEFAULT NULL,
  `build_server_type` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`build_server_environment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=434 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_repositories`
--

DROP TABLE IF EXISTS `configuration_repositories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_repositories` (
  `configuration_repository_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(450) DEFAULT NULL,
  `name` varchar(450) DEFAULT NULL,
  `url` varchar(450) DEFAULT NULL,
  `username` varchar(450) DEFAULT NULL,
  `password` varchar(450) DEFAULT NULL,
  `created_by` varchar(450) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `modifier` varchar(450) DEFAULT NULL,
  `updated_date` datetime DEFAULT NULL,
  PRIMARY KEY (`configuration_repository_id`),
  UNIQUE KEY `gots_id_name` (`gots_id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `deployment_config`
--

DROP TABLE IF EXISTS `deployment_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `deployment_config` (
  `deployment_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `pipeline_flow_id` int(11) DEFAULT NULL,
  `build_server_id` int(11) DEFAULT NULL,
  `environment_id` int(11) DEFAULT NULL,
  `configuration_repository_id` int(11) DEFAULT NULL,
  `name` varchar(450) DEFAULT NULL,
  `gots_id` varchar(45) DEFAULT NULL,
  `pipeline_name` varchar(450) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `updated_date` datetime DEFAULT NULL,
  `creator` varchar(45) DEFAULT NULL,
  `modifier` varchar(45) DEFAULT NULL,
  `description` blob,
  `enable_notification` bit(1) DEFAULT NULL,
  `notification_list` blob,
  `enable_approval` bit(1) DEFAULT NULL,
  `approval_list` blob,
  `deployment_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`deployment_config_id`),
  UNIQUE KEY `gotsid_pipelineflowid_name` (`gots_id`,`name`,`pipeline_flow_id`),
  KEY `pipeline_flow_id_idx` (`pipeline_flow_id`),
  KEY `build_server_id_idx` (`build_server_id`),
  KEY `environment_id_idx` (`environment_id`),
  KEY `configuration_repository_id_idx` (`configuration_repository_id`),
  CONSTRAINT `build_server_id` FOREIGN KEY (`build_server_id`) REFERENCES `build_server_environments` (`build_server_environment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `configuration_repository_id` FOREIGN KEY (`configuration_repository_id`) REFERENCES `configuration_repositories` (`configuration_repository_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `environment_id` FOREIGN KEY (`environment_id`) REFERENCES `environments` (`environment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pipeline_flow_id` FOREIGN KEY (`pipeline_flow_id`) REFERENCES `pipeline_flow` (`pipeline_flow_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eco_notification`
--

DROP TABLE IF EXISTS `eco_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eco_notification` (
  `eco_notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `link_name` varchar(4000) DEFAULT NULL,
  `title` varchar(4000) DEFAULT NULL,
  `type` varchar(45) DEFAULT NULL,
  `description` blob,
  `link` varchar(200) DEFAULT NULL,
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_active` varchar(20) DEFAULT NULL,
  `start_notification_date` datetime DEFAULT NULL,
  `end_notification_date` datetime DEFAULT NULL,
  PRIMARY KEY (`eco_notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eco_user_notification`
--

DROP TABLE IF EXISTS `eco_user_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eco_user_notification` (
  `eco_user_notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `eco_notification_id` int(11) DEFAULT NULL,
  `uuid` varchar(45) DEFAULT NULL,
  `read` varchar(20) DEFAULT NULL,
  `do_not_show` varchar(20) DEFAULT NULL,
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`eco_user_notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `environments`
--

DROP TABLE IF EXISTS `environments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `environments` (
  `environment_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(45) DEFAULT NULL,
  `environment_name` varchar(450) DEFAULT NULL,
  `cluster_name` varchar(450) DEFAULT NULL,
  `cluster_url` varchar(450) DEFAULT NULL,
  `username` varchar(450) DEFAULT NULL,
  `password` varchar(450) DEFAULT NULL,
  `environment_type` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`environment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=458 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gots_app_stats`
--

DROP TABLE IF EXISTS `gots_app_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gots_app_stats` (
  `gots_app_stats_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(45) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `pipeline_name` varchar(45) DEFAULT NULL,
  `submodule_name` varchar(45) DEFAULT NULL,
  `process_name` varchar(45) DEFAULT NULL,
  `process_type` varchar(45) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `assigned_to` varchar(500) DEFAULT NULL,
  `is_approved` varchar(45) DEFAULT NULL,
  `is_automatic` varchar(45) DEFAULT NULL,
  `process_id` varchar(45) DEFAULT NULL,
  `is_success` bit(1) DEFAULT NULL,
  `k8s_environment_url` varchar(450) DEFAULT NULL,
  `k8s_environment_type` varchar(450) DEFAULT NULL,
  `phase` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`gots_app_stats_id`)
) ENGINE=MyISAM AUTO_INCREMENT=583077 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_deployment_config`
--

DROP TABLE IF EXISTS `pipeline_deployment_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_deployment_config` (
  `pipeline_deployment_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `deployment_config_id` int(11) DEFAULT NULL,
  `gots_id` varchar(45) DEFAULT NULL,
  `name` varchar(450) DEFAULT NULL,
  `jenkins_job` varchar(450) DEFAULT NULL,
  `jenkins_params` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`pipeline_deployment_config_id`),
  UNIQUE KEY `pipelinedeployment_gots_pipeline` (`name`,`deployment_config_id`,`gots_id`),
  KEY `deployment_config_id_idx` (`deployment_config_id`),
  CONSTRAINT `deployment_config_id` FOREIGN KEY (`deployment_config_id`) REFERENCES `deployment_config` (`deployment_config_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=259 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_flow`
--

DROP TABLE IF EXISTS `pipeline_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_flow` (
  `pipeline_flow_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(45) DEFAULT NULL,
  `name` varchar(450) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `updated_date` datetime DEFAULT NULL,
  `creator` varchar(45) DEFAULT NULL,
  `modifier` varchar(45) DEFAULT NULL,
  `description` blob,
  PRIMARY KEY (`pipeline_flow_id`),
  UNIQUE KEY `gots_id_and_name` (`gots_id`,`name`),
  KEY `gots_id_idx` (`gots_id`),
  CONSTRAINT `gots_id` FOREIGN KEY (`gots_id`) REFERENCES `asf_gots` (`gots_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_jenkins_job`
--

DROP TABLE IF EXISTS `pipeline_jenkins_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_jenkins_job` (
  `pipeline_jenkins_job_id` int(11) NOT NULL AUTO_INCREMENT,
  `gots_id` varchar(45) DEFAULT NULL,
  `name` varchar(450) DEFAULT NULL,
  `pipeline_id` varchar(45) DEFAULT NULL,
  `execute_id` int(11) DEFAULT NULL,
  `pipeline_name` varchar(45) DEFAULT NULL,
  `startTime` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  `isSuccess` tinyint(4) DEFAULT NULL,
  `failReason` varchar(450) DEFAULT NULL,
  `phase` varchar(45) DEFAULT NULL,
  `env_type` varchar(45) DEFAULT NULL,
  `k8_env_url` varchar(450) DEFAULT NULL,
  `job_url` varchar(450) DEFAULT NULL,
  `assignTo` varchar(150) DEFAULT NULL,
  `deploy_config_name` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`pipeline_jenkins_job_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1994 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_jenkins_status`
--

DROP TABLE IF EXISTS `pipeline_jenkins_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_jenkins_status` (
  `gots_app_stats_id` int(11) DEFAULT NULL,
  `name` varchar(450) DEFAULT NULL,
  `status` varchar(45) DEFAULT NULL,
  `startTimeMillis` varchar(30) DEFAULT NULL,
  `durationMillis` varchar(30) DEFAULT NULL,
  `jenkins_status_id` int(11) NOT NULL AUTO_INCREMENT,
  `pipeline_jenkins_job_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`jenkins_status_id`)
) ENGINE=MyISAM AUTO_INCREMENT=11211 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_gots_config`
--

DROP TABLE IF EXISTS `pipeline_gots_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_gots_config` (
  `pipeline_gots_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `pipeline_config_id` int(11) DEFAULT NULL,
  `gots_id` varchar(45) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `attribute_value` varchar(4000) DEFAULT NULL,
  `array_indexer` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`pipeline_gots_config_id`),
  KEY `s_idx` (`pipeline_config_id`),
  KEY `pipeline_gots_config_gots_id_name_index` (`gots_id`,`name`),
  CONSTRAINT `pipeline_config_fk` FOREIGN KEY (`pipeline_config_id`) REFERENCES `pipeline_config` (`pipeline_config_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=124341 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pipeline_seed_info`
--

DROP TABLE IF EXISTS `pipeline_seed_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_seed_info` (
  `seed_id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `pipeline_name` varchar(45) DEFAULT NULL,
  `gots_id` varchar(45) NOT NULL,
  `seed_owner` varchar(50) DEFAULT NULL,
  `build_server_url` longtext,
  `source_code_url` longtext,
  `created_time` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`seed_id`,`name`,`gots_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `services_reports`
--

DROP TABLE IF EXISTS `services_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `services_reports` (
  `services_reports_id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(45) DEFAULT NULL,
  `gots_id` varchar(45) DEFAULT NULL,
  `start_time` varchar(45) DEFAULT NULL,
  `feature_name` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`services_reports_id`),
  KEY `gotsid` (`gots_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3210787 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_connections`
--

DROP TABLE IF EXISTS `system_connections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `system_connections` (
  `system_connections_id` int(11) NOT NULL AUTO_INCREMENT,
  `connection_name` varchar(45) DEFAULT NULL,
  `gots_id` varchar(45) DEFAULT NULL,
  `system_url` varchar(450) DEFAULT NULL,
  `system_username` varchar(450) DEFAULT NULL,
  `system_password` varchar(450) DEFAULT NULL,
  `system_type` varchar(450) DEFAULT NULL,
  PRIMARY KEY (`system_connections_id`)
) ENGINE=InnoDB AUTO_INCREMENT=917 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'camundabpmajsc6'
--

--
-- Dumping routines for database 'camundabpmajsc6'
--
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `clean_gots` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `clean_gots`(
	IN in_gots_id varchar(255)
)
BEGIN
	delete from camundabpmajsc6.asf_gots where gots_id = in_gots_id;
    delete from build_server_environments where gots_id = in_gots_id;
    delete from environments where gots_id = in_gots_id;
    delete from pipeline_gots_config where gots_id = in_gots_id;
    delete from system_connections where gots_id = in_gots_id;
    delete from gots_app_stats where gots_id = in_gots_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `gots_detail_report` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `gots_detail_report`(IN in_pipleline_name varchar(255), IN in_gots_id int)
BEGIN
SET SESSION group_concat_max_len = 1000000;
SET @sql = NULL;
SELECT GROUP_CONCAT(
DISTINCT CONCAT('SEC_TO_TIME(ROUND(AVG( Case when process_name=''', process_name,''' and subModule_name=''',subModule_name,''' 
THEN TIME_TO_SEC(TIMEDIFF(end_time, Start_time ))END ))) as ''', 
CASE WHEN (select distinct process_name_label from pipeline_config c where c.process_name=stats.process_name and c.submodule_name=stats.submodule_name) is NULL 
THEN process_name
ELSE (select distinct process_name_label from pipeline_config c where c.process_name=stats.process_name and c.submodule_name=stats.submodule_name) END
,'''' )
) 
INTO @sql
from gots_app_stats stats
where  pipeline_name=in_pipleline_name 
and gots_id= in_gots_id
and process_name !='' and submodule_name!='' and process_type!='notification'
order by gots_app_stats_id asc;

SET @sql= CONCAT('SELECT IT_LTM, gots.gots_id, pipeline_name, name as ''Pipeline Name'', ',
				  @sql, 
				 'from gots_app_stats stats
				  join asf_gots gots on gots.gots_id= stats.gots_id and gots.gots_id= ',in_gots_id,'
				  where pipeline_name=''',in_pipleline_name,'''
				  group by IT_LTM, gots.gots_id, pipeline_name,name');
 
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `gots_detail_summary_report` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `gots_detail_summary_report`(IN in_pipleline_name varchar(255), IN in_gots_id int)
BEGIN
SET SESSION group_concat_max_len = 1000000;
SET @sql = NULL;
SELECT GROUP_CONCAT(
DISTINCT CONCAT('SEC_TO_TIME(ROUND(AVG( Case when process_name=''', process_name,''' and subModule_name=''',subModule_name,''' 
THEN TIME_TO_SEC(TIMEDIFF(end_time, Start_time ))END ))) as ''', 
CASE WHEN (select distinct process_name_label from pipeline_config c where c.process_name=stats.process_name) is NULL 
THEN process_name
ELSE (select distinct process_name_label from pipeline_config c where c.process_name=stats.process_name) END
,'''' )
) 
INTO @sql
from gots_app_stats stats
where  pipeline_name=in_pipleline_name 
and gots_id= in_gots_id
and process_name !='' and submodule_name!='' and process_type!='notification'
order by gots_app_stats_id asc;

SET @sql= CONCAT('SELECT IT_LTM, gots.gots_id, pipeline_name, ',
				  @sql, 
				 'from gots_app_stats stats
				  join asf_gots gots on gots.gots_id= stats.gots_id and gots.gots_id= ',in_gots_id,'
				  where pipeline_name=''',in_pipleline_name,'''
				  group by IT_LTM, gots.gots_id, pipeline_name');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `gots_report` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `gots_report`(IN in_pipleline_name varchar(255),IN in_ltm_name varchar(255))
BEGIN
SET @sql = NULL;
SELECT GROUP_CONCAT(
DISTINCT CONCAT('SEC_TO_TIME(ROUND(AVG( Case when process_name=''', process_name,''' and subModule_name=''',subModule_name,''' 
THEN TIME_TO_SEC(TIMEDIFF(end_time, Start_time ))END ))) as ''', 
(select distinct submodule_label from pipeline_config c where c.submodule_name=stats.process_name)
,'''' )
) 
INTO @sql
from gots_app_stats stats
where  pipeline_name=in_pipleline_name 
and submodule_name=""
and process_name!='' 
order by gots_app_stats_id asc;

SET @sql= CONCAT('SELECT IT_LTM, gots.gots_id, gots.acronym, pipeline_name, ',
				  'SEC_TO_TIME(ROUND(AVG( Case when process_name='''' and subModule_name='''' THEN TIME_TO_SEC(TIMEDIFF(end_time, Start_time ))END ))) as ''Overall Time'' ,',
				  @sql, 
				 'from gots_app_stats stats
				  join asf_gots gots on gots.gots_id= stats.gots_id
				  where pipeline_name=''',in_pipleline_name,''' and
                  IT_LTM=''',in_ltm_name,'''
				  group by IT_LTM, gots.gots_id,acronym, pipeline_name');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `moveDownDeploymentConfig` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `moveDownDeploymentConfig`(
	IN in_pipeline_flow_id varchar(255),
    IN in_id varchar(255),
    IN in_gots_id varchar(255))
BEGIN
	SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
	START TRANSACTION;
		update deployment_config set deployment_order = deployment_order - 1
		where deployment_order = (select * from (select deployment_order + 1 from deployment_config
		where pipeline_flow_id=in_pipeline_flow_id and deployment_config_id=in_id and gots_id=in_gots_id) as t)
		and pipeline_flow_id=in_pipeline_flow_id and gots_id=in_gots_id;
		
		update deployment_config set deployment_order = deployment_order + 1 
		where pipeline_flow_id=in_pipeline_flow_id and deployment_config_id=in_id and gots_id=in_gots_id;
	COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `moveUpDeploymentConfig` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `moveUpDeploymentConfig`(
	IN in_pipeline_flow_id varchar(255),
    IN in_id varchar(255),
    IN in_gots_id varchar(255))
BEGIN
	SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
	START TRANSACTION;
		update deployment_config set deployment_order = deployment_order + 1
		where deployment_order = (select * from (select deployment_order - 1 from deployment_config
		where pipeline_flow_id=in_pipeline_flow_id and deployment_config_id=in_id and gots_id=in_gots_id) as t)
		and pipeline_flow_id=in_pipeline_flow_id and gots_id=in_gots_id;
		
		update deployment_config set deployment_order = deployment_order - 1 
		where pipeline_flow_id=in_pipeline_flow_id and deployment_config_id=in_id and gots_id=in_gots_id;
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `summary_report` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`camundabpmajsc6`@`%` PROCEDURE `summary_report`(IN in_pipleline_name varchar(255))
BEGIN
SET @sql = NULL;
SELECT GROUP_CONCAT(
DISTINCT CONCAT('SEC_TO_TIME(ROUND(AVG( Case when process_name=''', process_name,''' and subModule_name=''',subModule_name,''' 
THEN TIME_TO_SEC(TIMEDIFF(end_time, Start_time ))END ))) as ''', 
(select distinct submodule_label from pipeline_config c where c.submodule_name=stats.process_name)
,'''' )
) 
INTO @sql
from gots_app_stats stats
where  pipeline_name=in_pipleline_name 
and submodule_name=""
and process_name!='' 
order by gots_app_stats_id asc;

SET @sql= CONCAT('SELECT IT_LTM ,',
				  'SEC_TO_TIME(ROUND(AVG( Case when process_name='''' and subModule_name='''' THEN TIME_TO_SEC(TIMEDIFF(end_time, Start_time ))END ))) as ''Overall Time'' ,',
				  @sql, 
				 'from gots_app_stats stats
				  join asf_gots gots on gots.gots_id= stats.gots_id
				  where pipeline_name=''',in_pipleline_name,'''
				  group by IT_LTM');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

CREATE DATABASE  IF NOT EXISTS `camundabpmajsc6` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `camundabpmajsc6`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Database: camundabpmajsc6
-- ------------------------------------------------------
-- Server version	5.5.5-10.1.17-MariaDB

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
-- Table structure for table `pipeline_config`
--

DROP TABLE IF EXISTS `pipeline_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_config` (
  `pipeline_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `pipeline_name` varchar(45) DEFAULT NULL,
  `submodule_name` varchar(45) DEFAULT NULL,
  `process_name` varchar(45) DEFAULT NULL,
  `process_name_label` varchar(4500) DEFAULT NULL,
  `attribute_name` varchar(45) DEFAULT NULL,
  `attribute_type` varchar(45) DEFAULT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `label` varchar(4000) DEFAULT NULL,
  `submodule_label` varchar(45) DEFAULT NULL,
  `validation` varchar(45) DEFAULT NULL,
  `config_order` decimal(6,3) DEFAULT NULL,
  `config_type` varchar(45) DEFAULT NULL,
  `config_identifier` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`pipeline_config_id`),
  KEY `pipeline_config_config_order_index` (`config_order`)
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline_config`
--

LOCK TABLES `pipeline_config` WRITE;
/*!40000 ALTER TABLE `pipeline_config` DISABLE KEYS */;
INSERT INTO `pipeline_config` VALUES (1,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Jenkins Configuration','jenkinsURL','String','URL location for the project\'s main build.','URL','Developer and Assembly Testing','url',1.000,NULL,NULL),(2,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Jenkins Configuration','jenkinsUserName','String','Username to log into Jenkins','User Name','Developer and Assembly Testing',NULL,2.000,NULL,NULL),(3,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Jenkins Configuration','jenkinsPassword','String','Password to log in to Jenkins','Password','Developer and Assembly Testing','password',3.000,NULL,NULL),(4,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Jenkins Configuration','jenkinsEmailNotification','String','Email address to receive status notifications from Jenkins','Email Address','Developer and Assembly Testing','email',4.000,NULL,NULL),(5,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Git Hook Configuration','hasGitHook','Boolean','If enabled, the application will automatically trigger a release branch build','Enable','Developer and Assembly Testing','gitHook',5.000,NULL,NULL),(6,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Git Hook Configuration','gitHookUserName','String','User name for Git Hook authentication','User Name','Developer and Assembly Testing',NULL,6.000,NULL,NULL),(7,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Git Hook Configuration','gitHookPassword','String','Password for Git Hook authentication','Password','Developer and Assembly Testing','password',7.000,NULL,NULL),(8,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Environment Notification Email','environmentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the environment is ready.','Email Address','Developer and Assembly Testing','email',8.000,NULL,NULL),(9,'application-delivery-workflow-v1-0-2','dev-pipeline','global','Environment Notification Email','enableEnvironmentNotificationEmail','Boolean','Enable notification emails to receive environment notifications, such as when a process is beginning or ending','Enable','Developer and Assembly Testing',NULL,9.000,NULL,NULL),(10,'application-delivery-workflow-v1-0-2','dev-pipeline','RequestBuild','Request Build','assignTo','String','Provide an email address to request approval of this build','Email Address','Developer and Assembly Testing','email',10.000,NULL,NULL),(11,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','Trigger Release Branch Build','assignTo','String','Assign an email address to be responsible for triggering a release branch build','Email Address','Developer and Assembly Testing','email',11.000,NULL,NULL),(12,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','Trigger Release Branch Build','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Developer and Assembly Testing',NULL,12.000,NULL,NULL),(13,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','Trigger Release Branch Build','jobName','String','Provide the job name for the jenkins job to trigger release branch build','Jenkins Job Name','Developer and Assembly Testing',NULL,13.000,NULL,NULL),(14,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','Trigger Release Branch Build','jenkinsParams','String','Provide the parameters, if any, for the jenkins job to trigger release branch build','Jenkins Parameters','Developer and Assembly Testing','param',14.000,NULL,NULL),(15,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','Unit Test Execution','assignTo','String','Assign an email address to be responsible for unit testing','Email Address','Developer and Assembly Testing','email',15.000,NULL,NULL),(16,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','Unit Test Execution','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Developer and Assembly Testing',NULL,16.000,NULL,NULL),(17,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','Unit Test Execution','jobName','String','Provide the job name for the jenkins job for unit testing','Jenkins Job Name','Developer and Assembly Testing',NULL,17.000,NULL,NULL),(18,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','Unit Test Execution','jenkinsParams','String','Provide the parameters, if any, for jenkins unit testing','Jenkins Parameters','Developer and Assembly Testing','param',18.000,NULL,NULL),(19,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','Development Deployment','assignTo','String','Assign an email address to be responsible for development deployment','Email Address','Developer and Assembly Testing','email',19.000,NULL,NULL),(20,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','Development Deployment','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Developer and Assembly Testing',NULL,20.000,NULL,NULL),(21,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','Development Deployment','jobName','String','Provide the job name for the jenkins job for deploying into development environment','Jenkins Job Name','Developer and Assembly Testing',NULL,21.000,NULL,NULL),(22,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','Development Deployment','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy into development environment','Jenkins Parameters','Developer and Assembly Testing','param',22.000,NULL,NULL),(23,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','Environment Validation Testing','assignTo','String','Assign an email address to be responsible for environment validation testing','Email Address','Developer and Assembly Testing','email',23.000,NULL,NULL),(24,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','Environment Validation Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Developer and Assembly Testing',NULL,24.000,NULL,NULL),(25,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','Environment Validation Testing','jobName','String','Provide the job name for the jenkins job for environment validation testing','Jenkins Job Name','Developer and Assembly Testing',NULL,25.000,NULL,NULL),(26,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','Environment Validation Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to test environment validation','Jenkins Parameters','Developer and Assembly Testing','param',26.000,NULL,NULL),(27,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','Functional Testing','assignTo','String','Assign an email address to be responsible for functional test execution','Email Address','Developer and Assembly Testing','email',27.000,NULL,NULL),(28,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','Functional Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Developer and Assembly Testing',NULL,28.000,NULL,NULL),(29,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','Functional Testing','jobName','String','Provide the job name for the jenkins job to execute functional testing','Jenkins Job Name','Developer and Assembly Testing',NULL,29.000,NULL,NULL),(30,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','Functional Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute functional testing','Jenkins Parameters','Developer and Assembly Testing','param',30.000,NULL,NULL),(31,'application-delivery-workflow-v1-0-2','qc-pipeline','global','Jenkins Configuration','jenkinsURL','String','URL location for the project\'s main build','URL','Integrated System Testing / QC Testing',NULL,31.000,NULL,NULL),(32,'application-delivery-workflow-v1-0-2','qc-pipeline','global','Jenkins Configuration','jenkinsUserName','String','Username to log into Jenkins','User Name','Integrated System Testing / QC Testing',NULL,32.000,NULL,NULL),(33,'application-delivery-workflow-v1-0-2','qc-pipeline','global','Jenkins Configuration','jenkinsPassword','String','Password to log in to Jenkins','Password','Integrated System Testing / QC Testing','password',33.000,NULL,NULL),(34,'application-delivery-workflow-v1-0-2','qc-pipeline','global','Jenkins Configuration','jenkinsEmailNotification','String','Email address to receive status notifications from Jenkins','Email Address','Integrated System Testing / QC Testing','email',34.000,NULL,NULL),(35,'application-delivery-workflow-v1-0-2','qc-pipeline','global','Environment Notification Email','environmentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the environment is ready.','Email Address','Integrated System Testing / QC Testing','email',35.000,NULL,NULL),(36,'application-delivery-workflow-v1-0-2','qc-pipeline','global','Environment Notification Email','enableEnvironmentNotificationEmail','Boolean','Enable notification emails to receive environment notifications, such as when a process is beginning or ending','Enable','Integrated System Testing / QC Testing',NULL,36.000,NULL,NULL),(37,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestDeployment','Request Deployment','assignTo','String','Provide an email address to request deployment of this build','Email Address','Integrated System Testing / QC Testing','email',37.000,NULL,NULL),(38,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','Deploy To Environment','assignTo','String','Assign an email address to be responsible for deploying to environment','Email Address','Integrated System Testing / QC Testing','email',38.000,NULL,NULL),(39,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','Deploy To Environment','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Integrated System Testing / QC Testing',NULL,39.000,NULL,NULL),(40,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','Deploy To Environment','jobName','String','Provide the job name for the jenkins job to deploy to environment','Jenkins Job Name','Integrated System Testing / QC Testing',NULL,40.000,NULL,NULL),(41,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','Deploy To Environment','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy to environment','Jenkins Parameters','Integrated System Testing / QC Testing','param',41.000,NULL,NULL),(42,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','Environment Validation Testing','assignTo','String','Assign an email address to be responsible for environment validation testing','Email Address','Integrated System Testing / QC Testing','email',42.000,NULL,NULL),(43,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','Environment Validation Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Integrated System Testing / QC Testing',NULL,43.000,NULL,NULL),(44,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','Environment Validation Testing','jobName','String','Provide the job name for the jenkins job for environment validation testing','Jenkins Job Name','Integrated System Testing / QC Testing',NULL,44.000,NULL,NULL),(45,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','Environment Validation Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to test environment validation','Jenkins Parameters','Integrated System Testing / QC Testing','param',45.000,NULL,NULL),(46,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','Functional Testing','assignTo','String','Assign an email address to be responsible for functional test execution','Email Address','Integrated System Testing / QC Testing','email',46.000,NULL,NULL),(47,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','Functional Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Integrated System Testing / QC Testing',NULL,47.000,NULL,NULL),(48,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','Functional Testing','jobName','String','Provide the job name for the jenkins job to execute functional testing','Jenkins Job Name','Integrated System Testing / QC Testing',NULL,48.000,NULL,NULL),(49,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','Functional Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute functional testing','Jenkins Parameters','Integrated System Testing / QC Testing','param',49.000,NULL,NULL),(50,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestClientTestingApproval','Request Approval for Client Testing','assignTo','String','Provide an email address to assign the responsibility of approving the application into Client Testing','Email Address','Integrated System Testing / QC Testing','email',50.000,NULL,NULL),(51,'application-delivery-workflow-v1-0-2','client-testing-pipeline','global','Jenkins Configuration','jenkinsURL','String','URL location for the project\'s main build','URL','Client Testing',NULL,51.000,NULL,NULL),(52,'application-delivery-workflow-v1-0-2','client-testing-pipeline','global','Jenkins Configuration','jenkinsUserName','String','Username to log into Jenkins','User Name','Client Testing',NULL,52.000,NULL,NULL),(53,'application-delivery-workflow-v1-0-2','client-testing-pipeline','global','Jenkins Configuration','jenkinsPassword','String','Password to log in to Jenkins','Password','Client Testing','password',53.000,NULL,NULL),(54,'application-delivery-workflow-v1-0-2','client-testing-pipeline','global','Jenkins Configuration','jenkinsEmailNotification','String','Email address to receive status notifications from Jenkins','Email Address','Client Testing','email',54.000,NULL,NULL),(55,'application-delivery-workflow-v1-0-2','client-testing-pipeline','global','Environment Notification Email','environmentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the environment is ready.','Email Address','Client Testing','email',55.000,NULL,NULL),(56,'application-delivery-workflow-v1-0-2','client-testing-pipeline','global','Environment Notification Email','enableEnvironmentNotificationEmail','Boolean','Enable notification emails to receive environment notifications, such as when a process is beginning or ending','Enable','Client Testing',NULL,56.000,NULL,NULL),(57,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestDeploymentToClient','Request Deployment To Client','assignTo','String','Provide an email address to request deployment of this build to client environment','Email Address','Client Testing','email',57.000,NULL,NULL),(58,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','Deploy To Client','assignTo','String','Assign an email address to be responsible for deployment to client environment','Email Address','Client Testing','email',58.000,NULL,NULL),(59,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','Deploy To Client','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Client Testing',NULL,59.000,NULL,NULL),(60,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','Deploy To Client','jobName','String','Provide the job name for the jenkins job to deploy to client environment','Jenkins Job Name','Client Testing',NULL,60.000,NULL,NULL),(61,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','Deploy To Client','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy to client environment','Jenkins Parameters','Client Testing','param',61.000,NULL,NULL),(62,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','Environment Validation Testing','assignTo','String','Assign an email address to be responsible for environment validation testing','Email Address','Client Testing','email',62.000,NULL,NULL),(63,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','Environment Validation Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Client Testing',NULL,63.000,NULL,NULL),(64,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','Environment Validation Testing','jobName','String','Provide the job name for the jenkins job to execute environment validation testing','Jenkins Job Name','Client Testing',NULL,64.000,NULL,NULL),(65,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','Environment Validation Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute environment validation testing','Jenkins Parameters','Client Testing','param',65.000,NULL,NULL),(66,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestProductionTestingApproval','Request Production Testing','assignTo','String','Provide an email address to assign the responsibility of approving the application into Production Testing','Email Address','Client Testing','email',66.000,NULL,NULL),(67,'application-delivery-workflow-v1-0-2','production-pipeline','global','Jenkins Configuration','jenkinsURL','String','URL location for the project\'s main build','URL','Production Deployment',NULL,67.000,NULL,NULL),(68,'application-delivery-workflow-v1-0-2','production-pipeline','global','Jenkins Configuration','jenkinsUserName','String','Username to log into Jenkins','User Name','Production Deployment',NULL,68.000,NULL,NULL),(69,'application-delivery-workflow-v1-0-2','production-pipeline','global','Jenkins Configuration','jenkinsPassword','String','Password to log in to Jenkins','Password','Production Deployment','password',69.000,NULL,NULL),(70,'application-delivery-workflow-v1-0-2','production-pipeline','global','Jenkins Configuration','jenkinsEmailNotification','String','Email address to receive status notifications from Jenkins','Email Address','Production Deployment','email',70.000,NULL,NULL),(71,'application-delivery-workflow-v1-0-2','production-pipeline','global','Environment Notification Email','environmentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the environment is ready.','Email Address','Production Deployment','email',71.000,NULL,NULL),(72,'application-delivery-workflow-v1-0-2','production-pipeline','global','Environment Notification Email','enableEnvironmentNotificationEmail','Boolean','Enable notification emails to receive environment notifications, such as when a process is beginning or ending','Enable','Production Deployment',NULL,72.000,NULL,NULL),(73,'application-delivery-workflow-v1-0-2','production-pipeline','RequestDeploymentToProduction','Request Deployment to Production','assignTo','String','Provide an email address to request deployment of this build to production environment','Email Address','Production Deployment','email',73.000,NULL,NULL),(74,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','Deploy to Production','assignTo','String','Assign an email address to be responsible for deployment to production','Email Address','Production Deployment','email',74.000,NULL,NULL),(75,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','Deploy to Production','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Production Deployment',NULL,75.000,NULL,NULL),(76,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','Deploy to Production','jobName','String','Provide the job name for the jenkins job to deploy to production','Jenkins Job Name','Production Deployment',NULL,76.000,NULL,NULL),(77,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','Deploy to Production','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute deployment to production','Jenkins Parameters','Production Deployment','param',77.000,NULL,NULL),(78,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','Environment Validation Testing','assignTo','String','Assign an email address to be responsible for environment validation testing','Email Address','Production Deployment','email',78.000,NULL,NULL),(79,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','Environment Validation Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Production Deployment',NULL,79.000,NULL,NULL),(80,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','Environment Validation Testing','jobName','String','Provide the job name for the jenkins job to execute environment validation testing','Jenkins Job Name','Production Deployment',NULL,80.000,NULL,NULL),(81,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','Environment Validation Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute environment validation testing','Jenkins Parameters','Production Deployment','param',81.000,NULL,NULL),(82,'application-delivery-workflow-v1-0-2','production-pipeline','NotifyClientsOfDeployment','Notify Clients of Deployment','assignTo','String','Assign an email address to be responsible for notifying clients of deployment','Email Address','Production Deployment','email',82.000,NULL,NULL),(83,'maven-release-workflow-v1-0-2','maven-deploy','global','Jenkins Configuration','jenkinsURL','String','URL location for the project\'s main build','URL','Maven Development and Deployment','url',1.000,NULL,NULL),(84,'maven-release-workflow-v1-0-2','maven-deploy','global','Jenkins Configuration','jenkinsUserName','String','Username to log into Jenkins','User Name','Maven Development and Deployment',NULL,2.000,NULL,NULL),(85,'maven-release-workflow-v1-0-2','maven-deploy','global','Jenkins Configuration','jenkinsPassword','String','Password to log in to Jenkins','Password','Maven Development and Deployment','password',3.000,NULL,NULL),(86,'maven-release-workflow-v1-0-2','maven-deploy','global','Jenkins Configuration','jenkinsEmailNotification','String','Email address to receive status notifications from Jenkins','Email Address','Maven Development and Deployment','email',4.000,NULL,NULL),(87,'maven-release-workflow-v1-0-2','maven-deploy','global','Git Hook Configuration','hasGitHook','Boolean','If enabled, the application will automatically build a maven library','Enable','Maven Development and Deployment','gitHook',5.000,NULL,NULL),(88,'maven-release-workflow-v1-0-2','maven-deploy','global','Git Hook Configuration','gitHookUserName','String','User name for Git Hook authentication','User Name','Maven Development and Deployment',NULL,6.000,NULL,NULL),(89,'maven-release-workflow-v1-0-2','maven-deploy','global','Git Hook Configuration','gitHookPassword','String','Password for Git Hook authentication','Password','Maven Development and Deployment','password',7.000,NULL,NULL),(90,'maven-release-workflow-v1-0-2','maven-deploy','ApprovalByDevLead','Approval By Developer Lead','assignTo','String','Provide an email address to request approval from the developer lead','Email Address','Maven Development and Deployment','email',8.000,NULL,NULL),(91,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','Build Maven Library','assignTo','String','Assign an email address to be responsible for building the maven library','Email Address','Maven Development and Deployment','email',9.000,NULL,NULL),(92,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','Build Maven Library','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Development and Deployment',NULL,10.000,NULL,NULL),(93,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','Build Maven Library','jobName','String','Provide the job name for the jenkins job to build maven library','Jenkins Job Name','Maven Development and Deployment',NULL,11.000,NULL,NULL),(94,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','Build Maven Library','jenkinsParams','String','Provide the parameters, if any, for jenkins to build maven library','Jenkins Parameters','Maven Development and Deployment','param',12.000,NULL,NULL),(95,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','Unit Testing','assignTo','String','Assign an email address to be responsible for unit testing','Email Address','Maven Development and Deployment','email',13.000,NULL,NULL),(96,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','Unit Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Development and Deployment',NULL,14.000,NULL,NULL),(97,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','Unit Testing','jobName','String','Provide the job name for the jenkins job to execute unit testing','Jenkins Job Name','Maven Development and Deployment',NULL,15.000,NULL,NULL),(98,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','Unit Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute unit testing','Jenkins Parameters','Maven Development and Deployment','param',16.000,NULL,NULL),(99,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','Maven Snapshot Deployment','assignTo','String','Assign an email address to be responsible for maven snapshot deployment','Email Address','Maven Development and Deployment','email',17.000,NULL,NULL),(100,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','Maven Snapshot Deployment','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Development and Deployment',NULL,18.000,NULL,NULL),(101,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','Maven Snapshot Deployment','jobName','String','Provide the job name for the jenkins job to deploy a maven snapshot','Jenkins Job Name','Maven Development and Deployment',NULL,19.000,NULL,NULL),(102,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','Maven Snapshot Deployment','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy a maven snapshot','Jenkins Parameters','Maven Development and Deployment','param',20.000,NULL,NULL),(103,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','Deploy Library for End to End Testing','assignTo','String','Assign an email address to be responsible for deploying the library for end to end testing','Email Address','Maven Development and Deployment','email',21.000,NULL,NULL),(104,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','Deploy Library for End to End Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Development and Deployment',NULL,22.000,NULL,NULL),(105,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','Deploy Library for End to End Testing','jobName','String','Provide the job name for the jenkins job to deploy library for end to end testing','Jenkins Job Name','Maven Development and Deployment',NULL,23.000,NULL,NULL),(106,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','Deploy Library for End to End Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy the library for end to end testing','Jenkins Parameters','Maven Development and Deployment','param',24.000,NULL,NULL),(107,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','Execute End to End Testing','assignTo','String','Assign an email address to be responsible for executing end to end testing','Email Address','Maven Development and Deployment','email',25.000,NULL,NULL),(108,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','Execute End to End Testing','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Development and Deployment',NULL,26.000,NULL,NULL),(109,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','Execute End to End Testing','jobName','String','Provide the job name for the jenkins job to execute end to end testing','Jenkins Job Name','Maven Development and Deployment',NULL,27.000,NULL,NULL),(110,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','Execute End to End Testing','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute end to end testing','Jenkins Parameters','Maven Development and Deployment','param',28.000,NULL,NULL),(111,'maven-release-workflow-v1-0-2','maven-release','global','Jenkins Configuration','jenkinsURL','String','URL location for the project\'s main build','URL','Maven Release','url',29.000,NULL,NULL),(112,'maven-release-workflow-v1-0-2','maven-release','global','Jenkins Configuration','jenkinsUserName','String','Username to log into Jenkins','User Name','Maven Release',NULL,30.000,NULL,NULL),(113,'maven-release-workflow-v1-0-2','maven-release','global','Jenkins Configuration','jenkinsPassword','String','Password to log in to Jenkins','Password','Maven Release','password',31.000,NULL,NULL),(114,'maven-release-workflow-v1-0-2','maven-release','global','Jenkins Configuration','jenkinsEmailNotification','String','Email address to receive status notifications from Jenkins','Email Address','Maven Release','email',32.000,NULL,NULL),(115,'maven-release-workflow-v1-0-2','maven-release','ApprovalByTestLead','Approval By Test Lead','assignTo','String','Provide an email address to request approval from the test lead','Email Address','Maven Release','email',33.000,NULL,NULL),(116,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','Certify Maven Release','assignTo','String','Assign an email address to be responsible for certifying the maven release','Email Address','Maven Release','email',34.000,NULL,NULL),(117,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','Certify Maven Release','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Release',NULL,35.000,NULL,NULL),(118,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','Certify Maven Release','jobName','String','Provide the job name for the jenkins job to certify the maven release','Jenkins Job Name','Maven Release',NULL,36.000,NULL,NULL),(119,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','Certify Maven Release','jenkinsParams','String','Provide the parameters, if any, for jenkins to certify the maven release','Jenkins Parameters','Maven Release','param',37.000,NULL,NULL),(120,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','Maven Release','assignTo','String','Assign an email address to be responsible for the maven release','Email Address','Maven Release','email',38.000,NULL,NULL),(121,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','Maven Release','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Maven Release',NULL,39.000,NULL,NULL),(122,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','Maven Release','jobName','String','Provide the job name for the jenkins job to execute a maven release','Jenkins Job Name','Maven Release',NULL,40.000,NULL,NULL),(123,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','Maven Release','jenkinsParams','String','Provide the parameters, if any, to execute a maven release','Jenkins Parameters','Maven Release','param',41.000,NULL,NULL),(128,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Git Hook Configuration','hasGitHook','Boolean','If enabled, the application will automatically trigger a release branch build','Enable','Developer and Assembly Testing','gitHook',1.000,NULL,NULL),(129,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Git Hook Configuration','gitHookUserName','String','User name for Git Hook authentication','User Name','Developer and Assembly Testing',NULL,2.000,NULL,NULL),(130,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Git Hook Configuration','gitHookPassword','String','Password for Git Hook authentication','Password','Developer and Assembly Testing','password',3.000,NULL,NULL),(131,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Deployment Notification Email','deploymentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the deploymentis ready.','Email Address','Developer and Assembly Testing','email',4.000,'array','developmentLoop'),(132,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Deployment Notification Email','enableApproval','Boolean','Enable notification emails to receive deployment notifications, such as when a process is beginning or ending','Enable','Developer and Assembly Testing',NULL,5.000,'array','developmentLoop'),(133,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','RequestBuild','Request Build','assignTo','String','Provide an email address to request approval of this build','Assignee Email Address','Developer and Assembly Testing','email',6.000,'array','developmentLoop'),(134,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','Trigger Jenkins Development Pipeline','assignTo','String','Assign an email address to be responsible for triggering a release branch build','Assignee Email Address','Developer and Assembly Testing','email',7.000,'array','developmentLoop'),(135,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','Trigger Jenkins Development Pipeline','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Developer and Assembly Testing',NULL,8.000,'array','developmentLoop'),(136,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','Trigger Jenkins Development Pipeline','jobName','String','Provide the job name for the jenkins job to trigger release branch build','Jenkins Job Name','Developer and Assembly Testing',NULL,9.000,'array','developmentLoop'),(137,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','Trigger Jenkins Development Pipeline','jenkinsParams','String','Provide the parameters, if any, for the jenkins job to trigger release branch build','Jenkins Parameters','Developer and Assembly Testing','param',10.000,'array','developmentLoop'),(142,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','global','Deployment Notification Email','deploymentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the deploymentis ready.','Assignee Email Address','Integrated System Testing / QC Testing','email',11.000,'array','qcLoop'),(143,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','global','Deployment Notification Email','enableApproval','Boolean','Enable notification emails to receive deployment notifications, such as when a process is beginning or ending','Enable','Integrated System Testing / QC Testing',NULL,12.000,'array','qcLoop'),(144,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestDeployment','Request Deployment','assignTo','String','Provide an email address to request deployment of this build','Assignee Email Address','Integrated System Testing / QC Testing','email',13.000,'array','qcLoop'),(145,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','Trigger Jenkins QC Testing Pipeline','assignTo','String','Assign an email address to be responsible for deploying to environment','Assignee Email Address','Integrated System Testing / QC Testing','email',14.000,'array','qcLoop'),(146,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','Trigger Jenkins QC Testing Pipeline','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Integrated System Testing / QC Testing',NULL,15.000,'array','qcLoop'),(147,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','Trigger Jenkins QC Testing Pipeline','jobName','String','Provide the job name for the jenkins job to deploy to environment','Jenkins Job Name','Integrated System Testing / QC Testing',NULL,16.000,'array','qcLoop'),(148,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','Trigger Jenkins QC Testing Pipeline','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy to environment','Jenkins Parameters','Integrated System Testing / QC Testing','param',17.000,'array','qcLoop'),(149,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestClientTestingApproval','Request Approval for Client Testing','assignTo','String','Provide an email address to assign the responsibility of approving the application into Client Testing','Assignee Email Address','Integrated System Testing / QC Testing','email',18.000,NULL,NULL),(154,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','global','Deployment Notification Email','deploymentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the deployment is ready.','Email Address','Client Testing','email',19.000,'array','clientLoop'),(155,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','global','Deployment Notification Email','enableApproval','Boolean','Enable notification emails to receive deployment notifications, such as when a process is beginning or ending','Enable','Client Testing',NULL,20.000,'array','clientLoop'),(156,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestDeploymentToClient','Request Deployment To Client','assignTo','String','Provide an email address to request deployment of this build to client environment','Assignee Email Address','Client Testing','email',21.000,'array','clientLoop'),(157,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','Trigger Jenkins Client Testing Pipeline','assignTo','String','Assign an email address to be responsible for deployment to client environment','Assignee Email Address','Client Testing','email',22.000,'array','clientLoop'),(158,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','Trigger Jenkins Client Testing Pipeline','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Client Testing',NULL,23.000,'array','clientLoop'),(159,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','Trigger Jenkins Client Testing Pipeline','jobName','String','Provide the job name for the jenkins job to deploy to client environment','Jenkins Job Name','Client Testing',NULL,24.000,'array','clientLoop'),(160,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','Trigger Jenkins Client Testing Pipeline','jenkinsParams','String','Provide the parameters, if any, for jenkins to deploy to client environment','Jenkins Parameters','Client Testing','param',25.000,'array','clientLoop'),(161,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestProductionTestingApproval','Request Production Testing','assignTo','String','Provide an email address to assign the responsibility of approving the application into Production Testing','Assignee Email Address','Client Testing','email',26.000,NULL,NULL),(166,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','global','Deployment Notification Email','deploymentNotificationEmail','String','Designate one or more email address(es) to receive notifications when the deployment is ready.','Assignee Email Address','Production Deployment','email',27.000,'array','productionLoop'),(167,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','global','Deployment Notification Email','enableApproval','Boolean','Enable notification emails to receive deployment notifications, such as when a process is beginning or ending','Enable','Production Deployment',NULL,28.000,'array','productionLoop'),(168,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','RequestDeploymentToProduction','Request Deployment to Production','assignTo','String','Provide an email address to request deployment of this build to production environment','Assignee Email Address','Production Deployment','email',29.000,'array','productionLoop'),(169,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','Trigger Jenkins Production Testing Pipeline','assignTo','String','Assign an email address to be responsible for deployment to production','Assignee Email Address','Production Deployment','email',30.000,'array','productionLoop'),(170,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','Trigger Jenkins Production Testing Pipeline','automatic','Boolean','Select this option to run the Jenkins Job automatically','Automatic','Production Deployment',NULL,31.000,'array','productionLoop'),(171,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','Trigger Jenkins Production Testing Pipeline','jobName','String','Provide the job name for the jenkins job to deploy to production','Jenkins Job Name','Production Deployment',NULL,32.000,'array','productionLoop'),(172,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','Trigger Jenkins Production Testing Pipeline','jenkinsParams','String','Provide the parameters, if any, for jenkins to execute deployment to production','Jenkins Parameters','Production Deployment','param',33.000,'array','productionLoop'),(174,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Number of Deployments','developmentCounter','String','Select the number of environments','Amount','Developer and Assembly Testing','deploymentDropDown',3.100,'loop','developmentLoop'),(175,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','global','Deployment Name','deploymentName','String','The name for this deployment','Name','Developer and Assembly Testing',NULL,3.200,'array','developmentLoop'),(176,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','Trigger Jenkins Development Pipeline','buildServerDropDown','String','Select a Build Server','Name','Developer and Assembly Testing','buildServerDropDown',6.200,'array','developmentLoop'),(177,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','RequestBuild','Request Build','enableApproval','Boolean','Enable this option to receive notifications when approvals are needed','Require Approval','Developer and Assembly Testing',NULL,6.100,'array','developmentLoop'),(178,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','global','Number of Deployments','qcCounter','String','Select the number of environments','Amount','Integrated System Testing / QC Testing','deploymentDropDown',10.100,'loop','qcLoop'),(179,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','global','Deployment Name','deploymentName','String','The name for this deployment','Name','Integrated System Testing / QC Testing',NULL,10.200,'array','qcLoop'),(180,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','Trigger Jenkins Development Pipeline','environmentDropDown','String','Select an evironment','Environment','Developer and Assembly Testing','environmentDropDown',8.100,'array','developmentLoop'),(181,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','Trigger Jenkins QC Testing Pipeline','buildServerDropDown','String','Select a Build Server','Name','Integrated System Testing / QC Testing','buildServerDropDown',13.200,'array','qcLoop'),(182,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestDeployment','Request Deployment','enableApproval','Boolean','Enable this option to require the approval from the asignee','Require Approval','Integrated System Testing / QC Testing',NULL,13.100,'array','qcLoop'),(183,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','Trigger Jenkins QC Testing Pipeline','environmentDropDown','String','Select an evironment','Environment','Integrated System Testing / QC Testing','environmentDropDown',15.100,'array','qcLoop'),(184,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','global','Number of Deployments','clientCounter','String','Select the number of deployments','Amount','Client Testing','deploymentDropDown',18.100,'loop','clientLoop'),(185,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','global','Deployment Name','deploymentName','String','The name for this deployment','Name','Client Testing',NULL,18.200,'array','clientLoop'),(186,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','Trigger Jenkins Client Testing Pipeline','buildServerDropDown','String','Select a Build Server','Name','Client Testing','buildServerDropDown',21.200,'array','clientLoop'),(187,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestDeploymentToClient','Request Deployment To Client','enableApproval','Boolean','Enable this option to require the approval from the asignee','Require Approval','Client Testing',NULL,21.100,'array','clientLoop'),(188,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','Trigger Jenkins Client Testing Pipeline','environmentDropDown','String','Select an evironment','Environment','Client Testing','environmentDropDown',23.100,'array','clientLoop'),(189,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','global','Number of Deployments','productionCounter','String','Select the number of deployments','Amount','Production Deployment','deploymentDropDown',26.100,'loop','productionLoop'),(190,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','global','Deployment Name','deploymentName','String','The name for this deployment','Name','Production Deployment',NULL,26.200,'array','productionLoop'),(191,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','Trigger Jenkins Production Testing Pipeline','buildServerDropDown','String','Select a Build Server','Name','Production Deployment','buildServerDropDown',29.200,'array','productionLoop'),(192,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','RequestDeploymentToProduction','Request Deployment to Production','enableApproval','Boolean','Enable this option to require the approval from the asignee','Require Approval','Production Deployment',NULL,29.100,'array','productionLoop'),(193,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','Trigger Jenkins Production Testing Pipeline','environmentDropDown','String','Select an evironment','Environment','Production Deployment','environmentDropDown',31.100,'array','productionLoop'),(200,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestClientTestingApproval','Request Approval for Client Testing','enableApproval','Boolean','Select this option to enable approvals before the next step of the software life cycle','Require Approval','Integrated System Testing / QC Testing',NULL,18.010,NULL,NULL),(201,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestProductionTestingApproval','Request Production Testing','enableApproval','Boolean','Select this option to enable approvals before the next step of the software life cycle','Require Approval','Client Testing',NULL,26.010,NULL,NULL),(202,'cdp-workflow-v1','CDP Pipeline Workflow','global','Git Hook Configuration','hasGitHook','Boolean','If enabled, the application will automatically trigger a release branch build','Enable',NULL,'gitHook',1.000,NULL,NULL),(203,'cdp-workflow-v1','CDP Pipeline Workflow','global','Git Hook Configuration','gitHookUserName','String','User name for Git Hook authentication','User Name',NULL,NULL,2.000,NULL,NULL),(204,'cdp-workflow-v1','CDP Pipeline Workflow','global','Git Hook Configuration','gitHookPassword','String','Password for Git Hook authentication','Password',NULL,'password',3.000,NULL,NULL),(205,'cdp-workflow-v1','CDP Pipeline Workflow','global','Pipeline Flow','pipelineFlowDropDown','String','Select a pre-configured pipeline flow or create a new pipeline flow','Pipeline Flow',NULL,'pipelineFlowDropDown',4.000,NULL,NULL);
/*!40000 ALTER TABLE `pipeline_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pipeline_description`
--

DROP TABLE IF EXISTS `pipeline_description`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pipeline_description` (
  `pipeline_description_id` int(11) NOT NULL AUTO_INCREMENT,
  `pipeline_name` varchar(45) DEFAULT NULL,
  `submodule_name` varchar(45) DEFAULT NULL,
  `process_name` varchar(45) DEFAULT NULL,
  `attribute_name` varchar(45) DEFAULT NULL,
  `attribute_value` blob,
  PRIMARY KEY (`pipeline_description_id`)
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline_description`
--

LOCK TABLES `pipeline_description` WRITE;
/*!40000 ALTER TABLE `pipeline_description` DISABLE KEYS */;
INSERT INTO `pipeline_description` VALUES (1,'application-delivery-workflow-v1-0-2','dev-pipeline','RequestBuild','description','This step is for Development team to begin deployment to the development environment.  To start the process, select Approve below and Submit the request.'),(2,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','description','The Release Branch Build was configured to be handled manually, please click Submit when the task is finished.\n'),(3,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','description','Unit Testing was configured to be handled manually, please click Submit when the task is finished.\n'),(4,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','description','Development Deployment was configured to be handled manually, please click Submit when the task is finished.'),(5,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','description','Environment Validation Testing was configured to be handled manually, please click Submit when the task is finished.'),(6,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','description','Functional Testing was configured to be handled manually, please click Submit when the task is finished.'),(7,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestDeployment','description','This step is for Development team to Request Approval of Deployment.  To start the process, select Approve below and Submit the request.'),(8,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','description','Deployment to the Integrated System Testing Environment was configured to be handled manually, please click Submit when the task is finished.'),(9,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','description','Environment Validation Testing was configured to be handled manually, please click Submit when the task is finished.'),(10,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','description','Functional Testing was configured to be handled manually, please click Submit when the task is finished.'),(11,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestDeploymentToClient','description','Requesting Deployment to the Client Environment was configured to be handled manually, please click Submit when the task is finished.'),(12,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','description','Deployment to the Client Environment was configured to be handle manually, please click Submit when the task is finished.'),(13,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','description','Environment Validation Testing was configured to be handled manually, please click Submit when the task is finished.'),(14,'application-delivery-workflow-v1-0-2','client-testing-pipeline','NotifyClientsOfNewTestingNeed','description','Notifying Clients of new testing need was configured to be handled manually, please click Submit when the task is finished.'),(15,'application-delivery-workflow-v1-0-2','production-pipeline','RequestDeploymentToProduction','description','Requesting Deployment to Production was configured to be handled manually, please click Submit when the task is finished.'),(16,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','description','Deployment to Production was configured to be handled manually, please click Submit when the task is finished.'),(17,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','description','Environment Validation Testing was configured to be handled manually, please click Submit when the task is finished.'),(18,'application-delivery-workflow-v1-0-2','production-pipeline','NotifyClientsOfDeployment','description','Notifying Clients of Deployment was configured to be handled manually, please click Submit when the task is finished.'),(19,'maven-release-workflow-v1-0-2','maven-deploy','ApprovalByDevLead','description','This step is for Development team to Request Approval of a Code Build.  To start the process, select Approve below and Submit the request.'),(20,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','description','Building a Maven Library was configured to be handled manually, please click Submit when the task is finished.'),(21,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','description','Unit Testing was configured to be handled manually, please click Submit when the task is finished.'),(22,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','description','Deploying a Maven Snapshot was configured to be handled manually, please click Submit when the task is finished.'),(23,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','description','Deploying the Library for End to End Testing was configured to be handled manually, please click Submit when the task is finished.'),(24,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','description','End to End Test Execution was configured to be handled manually, please click Submit when the task is finished.'),(25,'maven-release-workflow-v1-0-2','maven-release','ApprovalByTestLead','description','This step is for the Development team to Request Approval of a Maven Release.  To start the process, select Approve below and Submit the request.'),(26,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','description','Certifying a Maven Release was configured to be handled manually, please click Submit when the task is finished.'),(27,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','description','Deploying a Maven Release was configured to be handled manually, please click Submit when the task is finished.'),(28,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','descriptionAutomatic','The Release Branch Build was configured to be handled automatically, please wait for the task to finish.\n'),(29,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','descriptionAutomatic','Unit Testing was configured to be handled automatically, please wait for the task to finish.'),(30,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','descriptionAutomatic','Development Deployment was configured to be handled automatically, please wait for the task to finish.'),(31,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','descriptionAutomatic','Environment Validation Testing was configured to be handled automatically, please wait until the task is finished.'),(32,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','descriptionAutomatic','Functional Testing was configured to be handled automatically, please wait until the task is finished.'),(33,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','descriptionAutomatic','Deployment to the Integrated System Testing Environment was configured to be handled automatically, please wait until the task is finished.'),(34,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','descriptionAutomatic','Environment Validation Testing was configured to be handled automatically, please wait until the task is finished.'),(35,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','descriptionAutomatic','Functional Testing was configured to be handled automatically, please wait until the task is finished.'),(36,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','descriptionAutomatic','Deployment to the Client Environment was configured to be handle automatically, please wait until the task is finished.'),(37,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','descriptionAutomatic','Environment Validation Testing was configured to be handled automatically, please wait until the task is finished.'),(38,'application-delivery-workflow-v1-0-2','client-testing-pipeline','NotifyClientsOfNewTestingNeed','descriptionAutomatic','Notifying Clients of new testing need was configured to be handled automatically, please wait until the task is finished.'),(39,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','descriptionAutomatic','Deployment to the Production Environment was configured to be handle automatically, please wait until the task is finished.'),(40,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','descriptionAutomatic','Environment Validation Testing was configured to be handled automatically, please wait until the task is finished.'),(41,'application-delivery-workflow-v1-0-2','production-pipeline','NotifyClientsOfDeployment','descriptionAutomatic','Notifying Clients of Deployment was configured to be handled automatically, please wait until the task is finished.'),(42,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','descriptionAutomatic','Building a Maven Library was configured to be handled automatically, please wait until the task is finished.'),(43,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','descriptionAutomatic','Unit Testing was configured to be handled automatically, please wait until the task is finished.'),(44,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','descriptionAutomatic','Deploying a Maven Snapshot was configured to be handled automatically, please wait until the task is finished.'),(45,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','descriptionAutomatic','Deploying the Library for End to End Testing was configured to be handled automatically, please wait until the task is finished.'),(46,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','descriptionAutomatic','End to End Test Execution was configured to be handled automatically, please wait until the task is finished.'),(47,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','descriptionAutomatic','Certifying a Maven Release was configured to be handled automatically, please wait until the task is finished.'),(48,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','descriptionAutomatic','Deploying a Maven Release was configured to be handled automatically, please wait until the task is finished.'),(49,'application-delivery-workflow-v1-0-2','dev-pipeline','RequestBuild','emailDescription','The Deployment Pipeline is currently at the Request Build stage.  To trigger a release branch build, please click on the link below.\n'),(50,'application-delivery-workflow-v1-0-2','dev-pipeline','TriggerReleaseBranchBuild','emailDescription','The Deployment Pipeline is currently at the Trigger Release Branch Build Stage which has previously been configured to be handled manually. To begin unit testing, please complete the current task and click the link below.'),(51,'application-delivery-workflow-v1-0-2','dev-pipeline','UnitTestExecution','emailDescription','The Deployment Pipeline is currently at the Unit Test Execution Stage which has previously been configured to be handled manually. To begin development deployment, please complete the current task and click the link below.'),(52,'application-delivery-workflow-v1-0-2','dev-pipeline','developmentDeployment','emailDescription','The Deployment Pipeline is currently at the Development Deployment Stage which has previously been configured to be handled manually. To begin environment validation testing, please complete the current task then click the link below.'),(53,'application-delivery-workflow-v1-0-2','dev-pipeline','environmentValidationTest','emailDescription','The Deployment Pipeline is currently at the Environment Validation Testing Stage which has previously been configured to be handled manually. To begin functional testing, please complete the current task then click the link below.'),(54,'application-delivery-workflow-v1-0-2','dev-pipeline','functionalTestExecution','emailDescription','The Deployment Pipeline is currently at the Functional Test Execution Stage which has previously been configured to be handled manually. To continue into integrated system testing, please complete the current task then click the link below.'),(55,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestDeployment','emailDescription','The Deployment Pipeline is currently at the Request Deployment stage.  To deploy to the test environment, please click on the link below.\n'),(56,'application-delivery-workflow-v1-0-2','qc-pipeline','DeployToEnvironment','emailDescription','The Deployment Pipeline is currently at the Deploy to Environment Stage which has previously been configured to be handled manually. To begin environment validation testing for the integrated system, please complete the current task then click the link below.'),(57,'application-delivery-workflow-v1-0-2','qc-pipeline','QCEnvironmentValidationTest','emailDescription','The Deployment Pipeline is currently at the Environment Validation Testing Stage which has previously been configured to be handled manually. To begin functional testing, please complete the current task then click the link below.'),(58,'application-delivery-workflow-v1-0-2','qc-pipeline','QCFunctionalTestExecution','emailDescription','The Deployment Pipeline is currently at the Functional Test Execution Stage which has previously been configured to be handled manually. To begin client testing, please complete the current task then click the link below.'),(59,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestDeploymentToClient','emailDescription','The Deployment Pipeline is currently at the Request Deployment stage.  To deploy to the client environment, please click on the link below.\n'),(60,'application-delivery-workflow-v1-0-2','client-testing-pipeline','DeployToClient','emailDescription','The Deployment Pipeline is currently at the Deploy to Client Environment Stage which has previously been configured to be handled manually. To begin environment validation testing for the client environment, please complete the current task then click the link below.'),(61,'application-delivery-workflow-v1-0-2','client-testing-pipeline','ClientEnvironmentValidationTest','emailDescription','The Deployment Pipeline is currently at the Environment Validation Testing Stage which has previously been configured to be handled manually. To request production testing approval, please complete the current task then click the link below.'),(62,'application-delivery-workflow-v1-0-2','client-testing-pipeline','NotifyClientsOfNewTestingNeed','emailDescription','The Deployment Pipeline is currently at the Notify Clients of New Testing Need, which has previously been configured to be handled manually. To request deployment to production, please complete the current task then click the link below.'),(63,'application-delivery-workflow-v1-0-2','production-pipeline','RequestDeploymentToProduction','emailDescription','The Deployment Pipeline is currently at the Request Deployment stage.  To deploy to the production environment, please click on the link below.\n'),(64,'application-delivery-workflow-v1-0-2','production-pipeline','DeployToProduction','emailDescription','The Deployment Pipeline is currently at the Deploy to Production Stage which has previously been configured to be handled manually. To begin environment validation testing for production environment, please complete the current task then click the link below.'),(65,'application-delivery-workflow-v1-0-2','production-pipeline','ProductionEnvionmentValidationTest','emailDescription','The Deployment Pipeline is currently at the Production Environment Validation Stage which has previously been configured to be handled manually. To notify clients of deployment, please complete the current task then click the link below.'),(66,'application-delivery-workflow-v1-0-2','production-pipeline','NotifyClientsOfDeployment','emailDescription','The Deployment Pipeline is currently at the Notifying Clients of Deployment Stage which has previously been configured to be handled manually. To deploy the application, please complete the current task then click the link below.'),(67,'maven-release-workflow-v1-0-2','maven-deploy','ApprovalByDevLead','emailDescription','The Deployment Pipeline is currently at the Request Build stage.  To Build a Maven Library, please click on the link below.'),(68,'maven-release-workflow-v1-0-2','maven-deploy','BuildMavenLibrary','emailDescription','The Deployment Pipeline is currently at the Build Maven Library stage, which has previously been configured to be handled manually.  To begin unit testing, please complete the task and click on the link below.\n'),(69,'maven-release-workflow-v1-0-2','maven-deploy','UnitTest','emailDescription','The Deployment Pipeline is currently at the Unit Testing stage, which has previously been configured to be handled manually.  To deploy a maven snapshot, please complete the task and click on the link below.\n'),(70,'maven-release-workflow-v1-0-2','maven-deploy','MavenSnapshotDeploy','emailDescription','The Deployment Pipeline is currently at the Maven Snapshot Deployment stage, which has previously been configured to be handled manually.  To deploy the library for end to end testing, please complete the task and click on the link below.\n'),(71,'maven-release-workflow-v1-0-2','maven-deploy','DeployLibraryForE2ETesting','emailDescription','The Deployment Pipeline is currently at the Deploy Library For End to End Testing stage, which has previously been configured to be handled manually.  To begin end to end testing, please complete the task and click on the link below.\n'),(72,'maven-release-workflow-v1-0-2','maven-deploy','ExecuteE2ETesting','emailDescription','The Deployment Pipeline is currently at the Execute End to End Testing stage, which has previously been configured to be handled manually.  To gain approval from a test lead to begin a Maven Relase, please complete the task and click on the link below.\n'),(73,'maven-release-workflow-v1-0-2','maven-release','ApprovalByTestLead','emailDescription','The Deployment Pipeline is currently at the Request Approval for a Maven Release stage.  To certify a Maven relase, please click on the link below.'),(74,'maven-release-workflow-v1-0-2','maven-release','CertifyMavenRelease','emailDescription','The Deployment Pipeline is currently at the Certify Maven Release stage, which has previously been configured to be handled manually.  To begin the process to deploy a maven release, please complete the task and click on the link below.\n'),(75,'maven-release-workflow-v1-0-2','maven-release','MavenRelease','emailDescription','The Deployment Pipeline is currently at the Maven Release stage, which has previously been configured to be handled manually.  To deploy a maven release, please complete the task and click on the link below.\n'),(76,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestClientTestingApproval','emailDescription','The Deployment Pipeline is currently at the Request Client Testing Approval stage. To approve deployment into Client Testing, please click on the link below.'),(77,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestClientTestingApproval','description','This step is for the Development team to approve deployment into the Client Testing Environment.  To start the process, select Approve below and Submit the request.'),(78,'application-delivery-workflow-v1-0-2','qc-pipeline','RequestClientTestingApproval','descriptionAutomatic','This step is for the Development team approve deployment into the Client Testing Environment.  To start the process, select Approve below and Submit the request.'),(79,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestProductionTestingApproval','emailDescription','The Deployment Pipeline is currently at the Request Production Testing Approval stage. To approve deployment into Production Testing, please click on the link below.'),(80,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestProductionTestingApproval','description','This step is for the Development team to approve deployment into the Production Testing Environment.  To start the process, select Approve below and Submit the request.'),(81,'application-delivery-workflow-v1-0-2','client-testing-pipeline','RequestProductionTestingApproval','descriptionAutomatic','This step is for the Development team to approve deployment into the Production Testing Environment.  To start the process, select Approve below and Submit the request.'),(83,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','RequestBuild','description','This step is for Development team to begin deployment to the development environment with deployment name \"((deployname))\".  To start the process, select Approve below and Submit the request.'),(84,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','description','The Jenkins Development Build with deployment name \"((deployname))\" was configured to be handled manually, please click Submit when the task is finished.\n'),(85,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestDeployment','description','This step is for the QC Test team to approve deployment into the QC Testing Environment with deployment name \"((deployname))\".  To start the process, select Approve below and Submit the request.'),(86,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','description','The Jenkins QC Build with deployment name \"((deployname))\" was configured to be handled manually, please click Submit when the task is finished.\n'),(87,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestClientTestingApproval','description','This step is for the QC Testing team to approve and start the Client Testing deployment process.  To start the process, select Approve below and Submit the request.'),(88,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestDeploymentToClient','description','This step is for the Client Test team to approve deployment into the Client Testing Environment with deployment name \"((deployname))\".  To start the process, select Approve below and Submit the request.'),(89,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','description','The Jenkins Client Testing Build with deployment name \"((deployname))\" was configured to be handled manually, please click Submit when the task is finished.\n'),(90,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestProductionTestingApproval','description','This step is for the Client Testing team to approve and start the Production deployment process.  To start the process, select Approve below and Submit the request.'),(91,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','RequestDeploymentToProduction','description','This step is for the Production Deployment team to approve deployment into the Production Environment with deployment name \"((deployname))\".  To start the process, select Approve below and Submit the request.'),(92,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','description','The Jenkins Production Build with deployment name \"((deployname))\" was configured to be handled manually, please click Submit when the task is finished.\n'),(93,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','RequestBuild','emailDescription','The deployment name \"((deploymentName))\" is currently at the Request Build stage.  To trigger the Jenkins development pipeline, please click the link below.\n'),(94,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','emailDescription','The deployment name \"((deploymentName))\" is currently at the Trigger Jenkins Development Stage which has previously been configured to be handled manually. To begin the deployment process, please click the link below and submit the task.'),(95,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestDeployment','emailDescription','The deployment name \"((deploymentName))\" is currently at the Request Deployment stage.  To trigger the QC testing pipeline, please click the link below.'),(96,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','emailDescription','The deployment name \"((deploymentName))\" is currently at the Trigger Jenkins QC Testing Stage which has previously been configured to be handled manually. To continue the deployment process, please click the link below and submit the task.'),(97,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestDeploymentToClient','emailDescription','The deployment name \"((deploymentName))\" is currently at the Request Deployment to Client stage.  To trigger the Client testing pipeline, please click the link below.'),(98,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','emailDescription','The deployment name \"((deploymentName))\" is currently at the Trigger Jenkins Client Testing Stage which has previously been configured to be handled manually. To continue the deployment process, please click the link below and submit the task.'),(99,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','RequestDeploymentToProduction','emailDescription','The deployment name \"((deploymentName))\" is currently at the Request Deployment to Production stage.  To trigger the Production testing pipeline, please click the link below.'),(100,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','emailDescription','The deployment name \"((deploymentName))\" is currently at the Trigger Jenkins Production Testing Stage which has previously been configured to be handled manually. To continue the deployment process, please click the link below and submit the task.'),(101,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','RequestClientTestingApproval','emailDescription','This step is for the QC Testing team to approve and start the Client Testing process. Please click the link below to approve the continuation of the process.'),(102,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','RequestProductionTestingApproval','emailDescription','This step is for the Client Testing team to approve and start the Production Testing process. Please click the link below to approve the continuation of the process.'),(105,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','descriptionAutomatic','The Jenkins Development Build with deployment name \"((deployname))\" was configured to be handled automatically, please wait until the task is finished.\n'),(106,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','descriptionAutomatic','The Jenkins QC Testing Build with deployment name \"((deployname))\" was configured to be handled automatically, please wait until the task is finished.\n'),(107,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','descriptionAutomatic','The Jenkins Client Testing Build with deployment name \"((deployname))\" was configured to be handled automatically, please wait until the task is finished.\n'),(108,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','descriptionAutomatic','The Jenkins Production Testing Build with deployment name \"((deployname))\" was configured to be handled automatically, please wait until the task is finished.\n'),(109,'jenkins-automated-deployment-workflow-v1','dev-pipeline-jenkins','triggerJenkinsDevelopment','descriptionFail','The Jenkins Development Build with deployment name \"((deployname))\" has failed. Please check your configurations and submit the task again.\n'),(110,'jenkins-automated-deployment-workflow-v1','qc-pipeline-jenkins','triggerJenkinsQCTesting','descriptionFail','The Jenkins QC Testing Build with deployment name \"((deployname))\" has failed. Please check your configurations and submit the task again.\n'),(111,'jenkins-automated-deployment-workflow-v1','client-testing-pipeline-jenkins','triggerJenkinsClientTesting','descriptionFail','The Jenkins Client Testing Build with deployment name \"((deployname))\" has failed. Please check your configurations and submit the task again.\n'),(112,'jenkins-automated-deployment-workflow-v1','production-pipeline-jenkins','triggerJenkinsProduction','descriptionFail','The Jenkins Production Testing Build with deployment name \"((deployname))\" has failed. Please check your configurations and submit the task again.\n'),(113,'cdp-workflow-v1','CDP Pipeline Workflow','Automate Build and Deploy','descriptionAutomatic','The deployment configuration \"((deployname))\" was configured to be handled automatically, please wait until the task is finished.'),(114,'cdp-workflow-v1','CDP Pipeline Workflow','Deployment Approval','description','This step is to begin the deployment process to the environment with deployment configuration name \"((deployname))\".  To start the process, select Approve below and Submit the request.'),(115,'cdp-workflow-v1','CDP Pipeline Workflow','Automate Build and Deploy','descriptionFail','The Jenkins Build with deployment configuration name \"((deployname))\" has failed. Please check your configurations and submit the task again.');
/*!40000 ALTER TABLE `pipeline_description` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_queries`
-- 

DROP TABLE IF EXISTS `report_queries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `report_queries` (
  `report_queries_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `query` blob,
  `query_description` blob,
  PRIMARY KEY (`report_queries_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_queries`
--

LOCK TABLES `report_queries` WRITE;
/*!40000 ALTER TABLE `report_queries` DISABLE KEYS */;
INSERT INTO `report_queries` VALUES (1,'Count Features','SELECT count(*) count , feature_name FROM services_reports group by feature_name;','Selects all feature names and the number of times each feature is called.'),(2,'Show Gots IDs','SELECT gots_id, Acronym, Application_name, Appl_Contact, IT_LTM FROM asf_gots order by IT_LTM;','Show the Gots IDs of Gots applications interacting with the application'),(3,'Show Count of Features by Gots','SELECT r.gots_id, m.Acronym, m.Application_name, feature_name, count(*) count FROM services_reports r\njoin asf_gots m on r.gots_id = m.gots_id\ngroup by r.gots_id, m.Acronym, m.Application_name, feature_name;','Show the number of times each feature was called within each Gots ID.'),(4,'Show Number Of Existing Pipelines Per Gots','select pmc.gots_id, asf.Acronym, asf.Application_Name, (select count(distinct(name))) count from pipeline_gots_config pmc\njoin asf_gots asf on pmc.gots_id = asf.gots_id\nwhere pmc.gots_id in (select asf.gots_id from asf_gots)\ngroup by gots_id;','Shows the number of currently configured pipelines per Gots ID.'),(5,'Number of Active Users Per Gots','select m.gots_id, m.Acronym, m.Application_name, count(distinct uuid) as \'Number of user\'\nFROM camundabpm.services_reports r\njoin camundabpm.asf_gots m on r.gots_id = m.gots_id\ngroup by m.gots_id,m.Acronym, m.Application_name order by count(distinct uuid) desc ;','Shows how many users have interacted with Eco per each gots ID.'),(6,'Eco Full Usage Report','SELECT \'Number of Users\' as \'Name\', count( distinct uuid) as \'Count\' FROM services_reports \nunion \nSELECT \'Number of gots Onboarded\' as \'Name\', count( distinct(gots_id)) as \'Count\' from asf_gots \nunion \nSELECT \'Number of Pipelines Created\' as \'Name\', count(distinct(CONCAT(gots_id, name))) as \'Count\' from pipeline_gots_config \nunion \nSELECT \'Number of Build Servers Configured\' as \'Name\' , count(distinct(CONCAT(gots_id, environment_name))) as \'Count\' from build_server_environments \nunion \nSELECT \'Number of Environments Configured\' as \'Name\', count(distinct(CONCAT(gots_id, environment_name))) as \'Count\' from environments \nunion \nSELECT \'Number of Repositories Configured\' as \'Name\', count(distinct(CONCAT(gots_id, connection_name))) as \'Count\' from system_connections \nunion \nSELECT \'Number of Pipeline Instances Started\' as \'Name\', count(distinct(process_id)) as \'Count\' FROM gots_app_stats \nunion \nSELECT \'Number of Jobs Ran Manually\' as \'Name\', count(*) as \'Count\' FROM gots_app_stats where process_type=\'jenkins\' and is_automatic=\'false\' \nunion \nSELECT \'Number of Jobs Ran Automatically\' as \'Name\' ,count(*) as \'Count\' FROM gots_app_stats where process_type=\'jenkins\' and is_automatic=\'true\' \nunion \nSELECT \'Number of Processes Approved\' as \'Name\', count(*) as \'Count\' FROM gots_app_stats where process_type=\'approval\' and is_approved=1 \nunion \nSELECT * FROM (SELECT \'Number of Automated Jobs Configured\' as \'Name\', count(*) as \'Count\' from pipeline_config pc join pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id where attribute_name=\'automatic\'  and attribute_value=\'true\' order by pc.config_order, pmc.array_indexer) as AutoJob \nunion \nSELECT * FROM (SELECT \'Number of Manual Jobs Configured\' as \'Name\', count(*) as \'Count\' from pipeline_config pc join pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id where attribute_name=\'automatic\' and attribute_value=\'false\'order by pc.config_order, pmc.array_indexer) as ManualJob;','Displays the number of users, gots, pipelines, build servers, environments, repositories, pipeline instances, and more.'),(7,'Pipeline Execution Metrics','select stats.gots_id, Acronym, Application_name, count(distinct(name)) as \'Number of Pipelines\', \ncount(distinct(process_id)) as \'Number of Pipelines Started\', count(*) as \'Number of Steps Executed\'\nfrom gots_app_stats stats\njoin asf_gots gots on gots.gots_id = stats.gots_id\ngroup by gots_id;','Displays the number of configured pipelines, how many pipelines were started, and how many steps were executed.'),(8,'Pipeline Execution Metrics per Day','select DATE_FORMAT(start_time, \'%m/%d/%Y\') as \'Date\', count(distinct(gots.gots_id)) as \'Number of gots Involved\',\ncount(distinct(process_id)) as \'Number of Pipelines Started\', count(*) as \'Number of Steps Executed\'\nfrom gots_app_stats stats\njoin asf_gots gots on gots.gots_id = stats.gots_id\ngroup by DATE_FORMAT(start_time, \'%m/%d/%Y\')\norder by start_time desc;','Displays the number of times pipelines were started and how many steps were executed per day.'),(9,'Number of Deployments to Production per gots','SELECT mas.gots_id, Acronym, Application_name, mas.`name` as pipelineName, count(distinct(process_id)) as \'Number of Production Deployments\'\nFROM gots_app_stats mas\njoin pipeline_gots_config pmc on pmc.gots_id = mas.gots_id\njoin asf_gots asf on mas.gots_id=asf.gots_id\nwhere mas.process_name=\'triggerJenkinsProduction\' and pipeline_config_id=\'172\' and pmc.attribute_value like \'PHASE=%DEPLOY%\' and end_time<>\'\' and end_time is not null\ngroup by gots_id , Acronym, Application_name, mas.`name`;','This report will find the number of times each gots deploys to a production environment in Kubernetes.'),(10,'Team Space Kubernetes Connection Report','select sc.gots_id, Acronym, Application_name, environment_name, username, cluster_url, environment_type  from environments sc\njoin asf_gots asf on asf.gots_id=sc.gots_id;','Displays which Kubernetes connections each Team Space has created'),(12,'Seed Certifications','select seed.display_name, (case when seed.type_tag=\'certified\' then seed.type_tag else \'not certified\' end) as Certification, catalog.owner from seedrepository.seed, seedrepository.catalog where seed.catalog_id=catalog.id;','Displays which seeds are certified.'),(13,'Team Space Build Server Connection Report','select sc.gots_id, Acronym, Application_name, environment_name, environment_url , username from build_server_environments sc\njoin asf_gots asf on asf.gots_id=sc.gots_id;','Displays which Team Spaces have configured which Build Server Connections'),(14,'Team Space Source Code Connection Report','select sc.gots_id, Acronym, Application_name, connection_name, system_url , system_username from SYSTEM_CONNECTIONS sc\njoin asf_gots asf on asf.gots_id=sc.gots_id and system_type=\'git\';','Displays which Team Spaces have saved which Source Code Connections'),(15,'Number of Deployments Per Day to All Environment','SELECT DATE(end_time) Date, COUNT( process_id) \'Total Deployments\'\nFROM gots_app_stats mas\njoin pipeline_gots_config pmc on pmc.gots_id = mas.gots_id and mas.name=pmc.name\nwhere (\n(mas.process_name=\'triggerJenkinsProduction\' and pipeline_config_id=\'172\') or\n(mas.process_name=\'triggerJenkinsDevelopment\' and pipeline_config_id=\'137\') or\n(mas.process_name=\'triggerJenkinsQCTesting\' and pipeline_config_id=\'148\') or\n(mas.process_name=\'triggerJenkinsClientTesting\' and pipeline_config_id=\'160\'))\nand pmc.attribute_value like \'PHASE=%DEPLOY%\' and end_time<>\'\' and end_time is not null\ngroup by DATE(end_time)\norder by DATE(end_time) desc;','Displays the total number of deployments to all Kubernetes environments per day'),(16,'Seed Generation Metrics per Seed','select seed.DISPLAY_NAME, seed_owner, count(seed_id) as \'Number of Services Generated\', count(distinct(gots_id)) as \'Number of gots\' from pipeline_seed_info info \njoin seedrepository.seed seed on info.seed_id = seed.id \ngroup by seed_id;','Displays the metrics for the number of services generated from a specific seed.'),(17,'gots ID and Kubernetes Environment Deployment Correlation','select  s.display_name as SeedName, asf.gots_id, asf.Acronym, asf.Application_name, mas.k8s_environment_url, mas.k8s_environment_type, mas.phase, mas.end_time\nfrom camundabpmajsc6.gots_app_stats mas\njoin asf_gots asf on asf.gots_id=mas.gots_id\nleft join pipeline_seed_info si on si.gots_id=asf.gots_id and si.name=mas.name and si.pipeline_name = mas.pipeline_name\nleft join seedrepository.seed s on s.id=si.seed_id\nwhere mas.process_type=\'jenkins\' and end_time<>\'\' and end_time is not null and phase is not null and phase<>\'\' and (phase=\'DEPLOY\' or phase=\'BUILD_DEPLOY\');','This report correlates gots IDs to which environments they have deployed to in Kubernetes.'),(18,'Jenkins Build Status','select seed.DISPLAY_NAME, startTime, endTime, TIMEDIFF(endTime,StartTime) as duration, j.pipeline_jenkins_job_id, j.gots_id, j.name, j.pipeline_id, j.execute_id, j.isSuccess ,s.name, s.status\n, failReason, env_type, phase, k8_env_url, job_url, assignTo\nfrom camundabpmajsc6.pipeline_jenkins_job j\nleft join camundabpmajsc6.pipeline_jenkins_status s\non s.pipeline_jenkins_job_id=j.pipeline_jenkins_job_id\nand jenkins_status_id = (select max(jenkins_status_id) from pipeline_jenkins_status s where s.pipeline_jenkins_job_id=j.pipeline_jenkins_job_id)\nleft join pipeline_seed_info psi on psi.name=j.name and psi.gots_id=j.gots_id\nleft join seedrepository.seed seed on seed.id = psi.seed_id\norder by pipeline_jenkins_job_id desc, jenkins_status_id;','Displays the build status for each pipeline job.');
/*!40000 ALTER TABLE `report_queries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES (10201);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `system_config` (
  `system_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `attribute_name` varchar(45) DEFAULT NULL,
  `attribute_value` blob,
  PRIMARY KEY (`system_config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'emailBody','<!--<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><div> <h2><span style=\"color:#4CA90C;\"> Eco</span></h2></div><hr/><h2 style=\"font-family:\'Arial\',\'sans-serif\';color:red;\">Action Requested for CICD Pipeline</h2><div style=\"font-size: 10.5pt; font-family: \'Arial\',\'sans-serif\';\"> <p>Hello ((emailTo)),</p><p>You have been assigned to approve the next step of the CICD Pipeline for Gots Application (((gotsid))), specifically the pipeline instance \'((name))\'.</p><p><strong>In order for this application to proceed, your approval is needed.</strong></p><p>The application is currently in ((submoduleName)) -> ((processName)).</p><p>Please click <a href=\"http://((clientURL))/uui#pipeline?id=((pipelineId))\">here</a> to approve this application through the next step of the CICD Pipeline.</p></div></html>--><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Basic</title><style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style></head> <body bgcolor=\"#FFFFFF\"><table class=\"head-wrap\" background=\"\"><tr><td></td><td class=\"header container\" ><div class=\"content\"><table><tr><td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td><td align=\"right\"><h6 class=\"collapse\"></h6></td></tr></table></div></td><td></td></tr></table><table class=\"body-wrap\"><tr><td></td><td class=\"container\" bgcolor=\"#FFFFFF\"><div class=\"content\"><table><tr><td><h3>Hi ((emailTo))</h3><p class=\"lead\">((acronym)) (((gotsid))) - The deployment name \"((deploymentName))\" requires actions to proceed to the next step. Any delay in this procedure could stop development, testing or deployment.</p><p>((emailDescription))</p><img src=\"http://((clientURL))/uui/public/email/images/camunda/((submoduleName)).png\"/><p>The application is currently in step: ((submoduleName)) -> ((processName)).</p><p><i>*Please reference the diagram above.</i></p><p class=\"callout\"><a href=\"http://((clientURL))/uui#pipeline?id=((pipelineId))\">Click here</a> to advance this application to the next step of the pipeline. </p></div></td><td></td></tr></table><table class=\"footer-wrap\"><tr><td></td><td class=\"container\"><div class=\"content\"><!---<table><tr><td align=\"center\"><p><a href=\"#\"></a><a href=\"#\"></a><a href=\"#\"></a></p></td></tr></table>--></div></td><td></td></tr></table></body></html>'),(2,'emailSubject','((acronym)) (((gotsid))) - Pending Actions for Pipeline Name: \"((name))\"'),(3,'exceptionBody','<!--<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><div> <h2><span style=\"color:#4CA90C;\"> Eco</span></h2></div><hr/><h2 style=\"font-family:\'Arial\',\'sans-serif\';color:red;\">Action Requested for CICD Pipeline</h2><div style=\"font-size: 10.5pt; font-family: \'Arial\',\'sans-serif\';\"> <p>Hello ((emailTo)),</p><p>You have been assigned to approve the next step of the CICD Pipeline for Gots Application (((gotsid))), specifically the pipeline instance \'((name))\'.</p><p><strong>In order for this application to proceed, your approval is needed.</strong></p><p>The application is currently in ((submoduleName)) -> ((processName)).</p><p>Please click <a href=\"http://((clientURL))/uui#pipeline?id=((pipelineId))\">here</a> to approve this application through the next step of the CICD Pipeline.</p></div></html>--><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Basic</title><style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style></head> <body bgcolor=\"#FFFFFF\"><table class=\"head-wrap\" background=\"\"><tr><td></td><td class=\"header container\" ><div class=\"content\"><table><tr><td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td><td align=\"right\"><h6 class=\"collapse\"></h6></td></tr></table></div></td><td></td></tr></table><table class=\"body-wrap\"><tr><td></td><td class=\"container\" bgcolor=\"#FFFFFF\"><div class=\"content\"><table><tr><td><h3>Hi ((emailTo))</h3><p class=\"lead\">((acronym)) (((gotsid))) - The deployment name \"((deploymentName))\" has encountered a build <span style=\"color:RED\">failure</span> with the Jenkins Job \"<span style=\"color:RED\">((jobName))</span>\".</p><img src=\"http://((clientURL))/uui/public/email/images/camunda/((submoduleName)).png\"/><p>The application is currently in step: ((submoduleName)) -> ((processName)).</p><p><i>*Please reference the diagram above.</i></p><p>((reason))</p><p class=\"callout\"><a href=\"http://((clientURL))/uui#pipeline?id=((pipelineId))\">Click here</a> to attempt to run this job again. </p><p>Additional information regarding this Jenkins Job Build:<br>Jenkins URL: ((jenkinsURL))<br>Jenkins User Name: ((jenkinsUserName))</p></div></td><td></td></tr></table><table class=\"footer-wrap\"><tr><td></td><td class=\"container\"><div class=\"content\"><!---<table><tr><td align=\"center\"><p><a href=\"#\"></a><a href=\"#\"></a><a href=\"#\"></a></p></td></tr></table>--></div></td><td></td></tr></table></body></html>'),(4,'exceptionSubject','((acronym)) (((gotsid))) - Jenkins Build Failure in CICD Pipeline for Pipeline Name: \"((name))\"'),(5,'jenkinsRefreshRate','PT15S'),(6,'jenkinsQueueExpireMins','10'),(7,'jenkinsJobStatusExpireMins','60'),(8,'environmentReadyEmailSubject','((acronym)) (((gotsid))) - Deployment name \"((deploymentName))\" has been Deployed and Tested for Pipeline Name: \"((name))\"'),(9,'environmentReadyEmailBody','<!--<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><div> <h2><span style=\"color:#4CA90C;\"> Eco</span></h2></div><hr/><h2 style=\"font-family:\'Arial\',\'sans-serif\';color:red;\">Action Requested for CICD Pipeline</h2><div style=\"font-size: 10.5pt; font-family: \'Arial\',\'sans-serif\';\"> <p>Hello ((emailTo)),</p><p>You have been assigned to approve the next step of the CICD Pipeline for Gots Application (((gotsid))), specifically the pipeline instance \'((name))\'.</p><p><strong>In order for this application to proceed, your approval is needed.</strong></p><p>The application is currently in ((submoduleName)) -> ((processName)).</p><p>Please click <a href=\"http://((clientURL))/uui#pipeline?id=((pipelineId))\">here</a> to approve this application through the next step of the CICD Pipeline.</p></div></html>--><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Basic</title><style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style></head> <body bgcolor=\"#FFFFFF\"><table class=\"head-wrap\" background=\"\"><tr><td></td><td class=\"header container\" ><div class=\"content\"><table><tr><td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td><td align=\"right\"><h6 class=\"collapse\"></h6></td></tr></table></div></td><td></td></tr></table><table class=\"body-wrap\"><tr><td></td><td class=\"container\" bgcolor=\"#FFFFFF\"><div class=\"content\"><table><tr><td><h3>Hi ((emailTo))</h3><p class=\"lead\">((acronym)) (((gotsid))) - The deployment name \"((deploymentName))\" has successfully been deployed and tested.</p><img src=\"http://((clientURL))/uui/public/email/images/camunda/((submoduleName)).png\"/></div></td><td></td></tr></table><table class=\"footer-wrap\"><tr><td></td><td class=\"container\"><div class=\"content\"><!---<table><tr><td align=\"center\"><p><a href=\"#\"></a><a href=\"#\"></a><a href=\"#\"></a></p></td></tr></table>--></div></td><td></td></tr></table></body></html>'),(10,'environmentPreBuildEmailSubject','((acronym)) (((gotsid))) - Deployment Notification for Pipeline Name: \"((name))\"'),(11,'environmentPreBuildEmailBody','<!--<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><div> <h2><span style=\"color:#4CA90C;\"> Eco</span></h2></div><hr/><h2 style=\"font-family:\'Arial\',\'sans-serif\';color:red;\">Action Requested for CICD Pipeline</h2><div style=\"font-size: 10.5pt; font-family: \'Arial\',\'sans-serif\';\"> <p>Hello ((emailTo)),</p><p>You have been assigned to approve the next step of the CICD Pipeline for Gots Application (((gotsid))), specifically the pipeline instance \'((name))\'.</p><p><strong>In order for this application to proceed, your approval is needed.</strong></p><p>The application is currently in ((submoduleName)) -> ((processName)).</p><p>Please click <a href=\"http://((clientURL))/uui#pipeline?id=((pipelineId))\">here</a> to approve this application through the next step of the CICD Pipeline.</p></div></html>--><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta name=\"viewport\" content=\"width=device-width\"/><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Basic</title><style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style></head> <body bgcolor=\"#FFFFFF\"><table class=\"head-wrap\" background=\"\"><tr><td></td><td class=\"header container\" ><div class=\"content\"><table><tr><td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td><td align=\"right\"><h6 class=\"collapse\"></h6></td></tr></table></div></td><td></td></tr></table><table class=\"body-wrap\"><tr><td></td><td class=\"container\" bgcolor=\"#FFFFFF\"><div class=\"content\"><table><tr><td><h3>Hi ((emailTo))</h3><p class=\"lead\">((acronym)) (((gotsid))) - The deployment name \"((deploymentName))\" is ready for ((processName)) and is about to be built. You will be notified when the deployment is complete. </div><img src=\"http://((clientURL))/uui/public/email/images/camunda/((submoduleName)).png\"/></td><td></td></tr></table><table class=\"footer-wrap\"><tr><td></td><td class=\"container\"><div class=\"content\"><!---<table><tr><td align=\"center\"><p><a href=\"#\"></a><a href=\"#\"></a><a href=\"#\"></a></p></td></tr></table>--></div></td><td></td></tr></table></body></html>'),(12,'taskCompleteBody','<!DOCTYPE html><html xmlns=http://www.w3.org/1999/xhtml><meta content=\"width=device-width\"name=viewport><meta content=\"text/html; charset=UTF-8\"http-equiv=Content-Type><title>Basic</title><style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\",Helvetica,Helvetica,Arial,sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100%!important;height:100%}a{color:#2ba6cb}.btn{display:inline-block;padding:6px 12px;margin-bottom:0;font-size:14px;font-weight:400;line-height:1.428571429;text-align:center;white-space:nowrap;vertical-align:middle;cursor:pointer;border:1px solid transparent;border-radius:4px;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;-o-user-select:none;user-select:none;color:#333;background-color:#fff;border-color:#CCC}p.callout{padding:15px;color:#8a6d3b;background-color:#fcf8e3;border-color:#faebcc;margin-bottom:15px;border:1px solid transparent;border-radius:4px}.callout a{font-weight:700;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:700;display:block;text-align:center}a.fb{background-color:#3b5998!important}a.tw{background-color:#1daced!important}a.gp{background-color:#db4a39!important}a.ms{background-color:#000!important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both!important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:700}h1,h2,h3,h4,h5,h6{font-family:HelveticaNeue-Light,\"Helvetica Neue Light\",\"Helvetica Neue\",Helvetica,Arial,\"Lucida Grande\",sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small,h2 small,h3 small,h4 small,h5 small,h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0!important}p,ul{margin-bottom:10px;font-weight:400;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1,ul.sidebar li a h2,ul.sidebar li a h3,ul.sidebar li a h4,ul.sidebar li a h5,ul.sidebar li a h6,ul.sidebar li a p{margin-bottom:0!important}.container{display:block!important;max-width:600px!important;margin:0 auto!important;clear:both!important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0!important;margin:0 auto;max-width:600px!important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=btn]{display:block!important;margin-bottom:10px!important;background-image:none!important;margin-right:0!important}div[class=column]{width:auto!important;float:none!important}table.social div[class=column]{width:auto!important}}</style><body bgcolor=#FFFFFF><table class=head-wrap background=><tr><td><td class=\"container header\"><div class=content><table><tr><td><img src=http://((clientURL))/uui/public/email/images/eco_email_logo.jpg height=43 width=177><td align=right><h6 class=collapse></h6></table></div><td></table><table class=body-wrap><tr><td><td class=container bgcolor=#FFFFFF><div class=content><table><tr><td><h3>Hi ((emailTo))</h3><p class=lead>((acronym)) (((gotsid))) - The deployment name \"((deploymentName))\" has been approved to move on to the next step of the pipeline by <span style=\"color:#0645AD;text-decoration:underline;\">((approver))</span>.</p><img src=http://((clientURL))/uui/public/email/images/camunda/((submoduleName)).png><p>The deployment name \"((deploymentName))\" was approved to continue from the step: ((submoduleName)) -> ((processName)).<p><i>*Please reference the diagram above.</i><p>You will be notified when further action is required.</div><td></table><table class=footer-wrap><tr><td><td class=container><div class=content></div><td></table>'),(13,'taskCompleteSubject','((acronym)) (((gotsid))) - Task Completed in Eco for the Pipeline \"((name))\"'),(14,'gotsApplicationContactRequest','<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"> <head> <meta name=\"viewport\" content=\"width=device-width\"/> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/> <title>Basic</title> <style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style> </head> <body bgcolor=\"#FFFFFF\"> <table class=\"head-wrap\" background=\"\"> <tr> <td></td><td class=\"header container\" > <div class=\"content\"> <table> <tr> <td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td><td align=\"right\"> <h6 class=\"collapse\"></h6> </td></tr></table> </div></td><td></td></tr></table> <table class=\"body-wrap\"> <tr> <td></td><td class=\"container\" bgcolor=\"#FFFFFF\"> <div class=\"content\"> <table> <tr> <td> <h3>Hi ((appContact)),</h3> <p class=\"lead\">An Eco user has requested to register Gots Application ID: <strong>((gotsid))</strong>, for which you are listed as the Application Contact in Gots.<br>Please review the following request and register Gots Application ID <strong>((gotsid))</strong> in Eco, per your discretion.</p><p>Requester Name: <strong>((requesterName))</strong><br>Requester UID: <strong>((requesterId))</strong><br>Reason: ((reason))</p></div></td><td></td></tr></table> <table class=\"footer-wrap\"> <tr> <td></td><td class=\"container\"> <div class=\"content\"> </div></td><td></td></tr></table> </body></html>'),(15,'gotsApplicationContactRequestSubject','Eco: Request to Register Gots Appliction ID ((gotsid))'),(16,'administratorAccessRequest','<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"> <head> <meta name=\"viewport\" content=\"width=device-width\"/> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/> <title>Basic</title> <style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style> </head> <body bgcolor=\"#FFFFFF\"> <table class=\"head-wrap\" background=\"\"> <tr> <td></td><td class=\"header container\" > <div class=\"content\"> <table> <tr> <td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td><td align=\"right\"> <h6 class=\"collapse\"></h6> </td></tr></table> </div></td><td></td></tr></table> <table class=\"body-wrap\"> <tr> <td></td><td class=\"container\" bgcolor=\"#FFFFFF\"> <div class=\"content\"> <table> <tr> <td> <h3>Hi ((emailTo))</h3> <p class=\"lead\">An Eco user has requested to be added as an <strong>administrator</strong> to your Eco Application: <strong>((acronym)) (((gotsid)))</strong>.<br>Please add the following user to Eco Application <strong>((acronym)) (((gotsid)))</strong>, per your discretion.</p><p>Requester Name: <strong>((requesterName))</strong><br>Requester UID: <strong>((requesterId))</strong><br>Reason: ((reason))</p><p class=\"callout\"><a href=\"http://((clientURL))/uui/landing\">Click here</a> to log in to Eco. </p></div></td><td></td></tr></table> <table class=\"footer-wrap\"> <tr> <td></td><td class=\"container\"> <div class=\"content\"> </div></td><td></td></tr></table> </body></html>'),(17,'administratorAccessRequestSubject','Eco: Request to add user \"((requesterId))\" to ((acronym)) (((gotsid)))'),(18,'jenkinsRefreshRateSeconds','2'),(19,'productionRestricted','\n\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n   <head>\n      <meta name=\"viewport\" content=\"width=device-width\"/>\n      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n      <title>Basic</title>\n      <style>*{margin:0;padding:0}*{font-family:\"Helvetica Neue\", \"Helvetica\", Helvetica, Arial, sans-serif}img{max-width:100%}.collapse{margin:0;padding:0}body{-webkit-font-smoothing:antialiased;-webkit-text-size-adjust:none;width:100% !important;height:100%}a{color:#2ba6cb}.btn{display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;color: #333;background-color: white;border-color: #CCC;}p.callout{padding:15px;color: #8a6d3b; background-color: #fcf8e3; border-color: #faebcc;margin-bottom:15px;border: 1px solid transparent; border-radius: 4px;}.callout a{font-weight:bold;color:#2ba6cb}table.social{background-color:#ebebeb}.social .soc-btn{padding:3px 7px;border-radius:2px;-webkit-border-radius:2px;-moz-border-radius:2px;font-size:12px;margin-bottom:10px;text-decoration:none;color:#FFF;font-weight:bold;display:block;text-align:center}a.fb{background-color:#3b5998 !important}a.tw{background-color:#1daced !important}a.gp{background-color:#db4a39 !important}a.ms{background-color:#000 !important}.sidebar .soc-btn{display:block;width:100%}table.head-wrap{width:100%;background-color:#EBEBEB;}.header.container table td.logo{padding:15px}.header.container table td.label{padding:15px;padding-left:0}table.body-wrap{width:100%}table.footer-wrap{width:100%;clear:both !important}.footer-wrap .container td.content p{border-top:1px solid #d7d7d7;padding-top:15px}.footer-wrap .container td.content p{font-size:10px;font-weight:bold}h1, h2, h3, h4, h5, h6{font-family:\"HelveticaNeue-Light\", \"Helvetica Neue Light\", \"Helvetica Neue\", Helvetica, Arial, \"Lucida Grande\", sans-serif;line-height:1.1;margin-bottom:15px;color:#000}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size:60%;color:#6f6f6f;line-height:0;text-transform:none}h1{font-weight:200;font-size:44px}h2{font-weight:200;font-size:37px}h3{font-weight:500;font-size:27px}h4{font-weight:500;font-size:23px}h5{font-weight:900;font-size:17px}h6{font-weight:900;font-size:14px;text-transform:uppercase;color:#444}.collapse{margin:0 !important}p, ul{margin-bottom:10px;font-weight:normal;font-size:14px;line-height:1.6}p.lead{font-size:17px}p.last{margin-bottom:0}ul li{margin-left:5px;list-style-position:inside}ul.sidebar{background:#ebebeb;display:block;list-style-type:none}ul.sidebar li{display:block;margin:0}ul.sidebar li a{text-decoration:none;color:#666;padding:10px 16px;margin-right:10px;cursor:pointer;border-bottom:1px solid #777;border-top:1px solid #fff;display:block;margin:0}ul.sidebar li a.last{border-bottom-width:0}ul.sidebar li a h1, ul.sidebar li a h2, ul.sidebar li a h3, ul.sidebar li a h4, ul.sidebar li a h5, ul.sidebar li a h6, ul.sidebar li a p{margin-bottom:0 !important}.container{display:block !important;max-width:600px !important;margin:0 auto !important;clear:both !important}.content{padding:15px;max-width:600px;margin:0 auto;display:block}.content table{width:100%}.column{width:300px;float:left}.column tr td{padding:15px}.column-wrap{padding:0 !important;margin:0 auto;max-width:600px !important}.column table{width:100%}.social .column{width:280px;min-width:279px;float:left}.clear{display:block;clear:both}@media only screen and (max-width:600px){a[class=\"btn\"]{display:block !important;margin-bottom:10px !important;background-image:none !important;margin-right:0 !important}div[class=\"column\"]{width:auto !important;float:none !important}table.social div[class=\"column\"]{width:auto !important}}</style>\n   </head>\n   <body bgcolor=\"#FFFFFF\">\n      <table class=\"head-wrap\" background=\"\">\n         <tr>\n            <td></td>\n            <td class=\"header container\" >\n               <div class=\"content\">\n                  <table>\n                     <tr>\n                        <td><img height=\"43\" width=\"177\" src=\"http://((clientURL))/uui/public/email/images/eco_email_logo.jpg\"/></td>\n                        <td align=\"right\">\n                           <h6 class=\"collapse\"></h6>\n                        </td>\n                     </tr>\n                  </table>\n               </div>\n            </td>\n            <td></td>\n         </tr>\n      </table>\n      <table class=\"body-wrap\">\n      <tr>\n         <td></td>\n         <td class=\"container\" bgcolor=\"#FFFFFF\">\n            <div class=\"content\">\n               <table>\n                  <tr>\n                     <td>\n                        <h3>Hi ((emailTo))</h3>\n                        <p class=\"lead\">The pipeline name \"((name))\" has been <strong>prevented</strong> from deploying into a production environment.\n                        <p>In order to configure and deploy into a production environment within Eco, you must associate your project to a Gots ID.</p>\n                        <p class=\"callout\"><a href=\"http://((clientURL))/uui/landing\">Click here</a> to log in to Eco. </p>\n                        \n            </div>\n            </td><td></td></tr></table> \n            <table class=\"footer-wrap\">\n               <tr>\n                  <td></td>\n                  <td class=\"container\">\n                     <div class=\"content\"> </div>\n                  </td>\n                  <td></td>\n               </tr>\n            </table>\n   </body>\n</html>\n\n'),(20,'productionRestrictedSubject','(((acronym))) - Production Deployment Prevented');/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-11 11:38:17

DROP TABLE IF EXISTS `login_info`; 
CREATE TABLE `login_info` (
  `username` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `aafprofile` varchar(255) DEFAULT NULL, 
  `pass` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;