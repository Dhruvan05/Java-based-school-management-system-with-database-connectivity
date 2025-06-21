-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Jun 21, 2025 at 10:15 AM
-- Server version: 8.0.42
-- PHP Version: 8.0.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `school_management_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
CREATE TABLE IF NOT EXISTS `course` (
  `courseId` int NOT NULL AUTO_INCREMENT,
  `courseCode` varchar(20) NOT NULL,
  `courseName` varchar(100) NOT NULL,
  `description` text,
  `credits` int NOT NULL,
  `capacity` int NOT NULL,
  `teacherId` int DEFAULT NULL,
  PRIMARY KEY (`courseId`),
  UNIQUE KEY `courseCode` (`courseCode`),
  KEY `teacherId` (`teacherId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `course`
--

INSERT INTO `course` (`courseId`, `courseCode`, `courseName`, `description`, `credits`, `capacity`, `teacherId`) VALUES
(1, 'MATH101', 'Calculus I', 'Introduction to differential and integral calculus.', 5, 30, 1),
(2, 'SCI201', 'Physics Fundamentals', 'Basic principles of physics.', 4, 25, 2),
(3, 'ENG301', 'Literary Analysis', 'Advanced study of literary texts.', 3, 20, 3),
(5, 'CS101', 'Introduction to Computer Science', '', 4, 33, 7);

-- --------------------------------------------------------

--
-- Table structure for table `courseschedule`
--

DROP TABLE IF EXISTS `courseschedule`;
CREATE TABLE IF NOT EXISTS `courseschedule` (
  `scheduleId` int NOT NULL AUTO_INCREMENT,
  `courseId` int NOT NULL,
  `timeSlotId` int NOT NULL,
  PRIMARY KEY (`scheduleId`),
  UNIQUE KEY `courseId` (`courseId`,`timeSlotId`),
  KEY `timeSlotId` (`timeSlotId`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `courseschedule`
--

INSERT INTO `courseschedule` (`scheduleId`, `courseId`, `timeSlotId`) VALUES
(1, 1, 4),
(2, 2, 1),
(3, 3, 3),
(4, 5, 4);

-- --------------------------------------------------------

--
-- Table structure for table `enrollment`
--

DROP TABLE IF EXISTS `enrollment`;
CREATE TABLE IF NOT EXISTS `enrollment` (
  `enrollmentId` int NOT NULL AUTO_INCREMENT,
  `studentId` int NOT NULL,
  `courseId` int NOT NULL,
  `enrollmentDate` date NOT NULL,
  `grade` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`enrollmentId`),
  UNIQUE KEY `studentId` (`studentId`,`courseId`),
  KEY `courseId` (`courseId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `enrollment`
--

INSERT INTO `enrollment` (`enrollmentId`, `studentId`, `courseId`, `enrollmentDate`, `grade`) VALUES
(1, 1, 1, '2023-09-01', 'A'),
(2, 2, 2, '2023-09-02', 'B+'),
(3, 3, 1, '2023-09-01', 'C');

-- --------------------------------------------------------

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
CREATE TABLE IF NOT EXISTS `student` (
  `studentId` int NOT NULL AUTO_INCREMENT,
  `firstName` varchar(50) NOT NULL,
  `lastName` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `dateOfBirth` date DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `phoneNumber` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`studentId`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `student`
--

INSERT INTO `student` (`studentId`, `firstName`, `lastName`, `email`, `dateOfBirth`, `address`, `phoneNumber`) VALUES
(1, 'David', 'Brown', 'david.brown@example.com', '2005-01-15', '123 Main St, Anytown', '555-111-2222'),
(2, 'Emily', 'Davis', 'emily.davis@example.com', '2004-05-22', '456 Oak Ave, Somewhere', '555-333-4444'),
(3, 'Frank', 'Miller', 'frank.miller@example.com', '2006-11-01', '789 Pine Ln, Nowhere', '555-555-6666');

-- --------------------------------------------------------

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
CREATE TABLE IF NOT EXISTS `teacher` (
  `teacherId` int NOT NULL AUTO_INCREMENT,
  `firstName` varchar(50) NOT NULL,
  `lastName` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `department` varchar(100) DEFAULT NULL,
  `phoneNumber` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`teacherId`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `teacher`
--

INSERT INTO `teacher` (`teacherId`, `firstName`, `lastName`, `email`, `department`, `phoneNumber`) VALUES
(1, 'Alice', 'Smith', 'alice.smith@example.com', 'Mathematics', '111-222-3333'),
(2, 'Bob', 'Johnson', 'bob.johnson@example.com', 'Science', '444-555-6666'),
(3, 'Carol', 'Williams', 'carol.williams@example.com', 'Literature', '777-888-9999'),
(4, 'David', 'Miller', 'david.miller@example.com', 'Physics', '888-111-2222'),
(5, 'Eva', 'Brown', 'eva.brown@example.com', 'Chemistry', '999-222-3333'),
(6, 'Frank', 'Garcia', 'frank.garcia@example.com', 'Biology', '666-333-4444'),
(7, 'Grace', 'Lee', 'grace.lee@example.com', 'Computer Science', '555-444-1111'),
(8, 'Henry', 'Clark', 'henry.clark@example.com', 'History', '333-666-7777');

-- --------------------------------------------------------

--
-- Table structure for table `timeslot`
--

DROP TABLE IF EXISTS `timeslot`;
CREATE TABLE IF NOT EXISTS `timeslot` (
  `timeSlotId` int NOT NULL AUTO_INCREMENT,
  `dayOfWeek` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
  `startTime` time NOT NULL,
  `endTime` time NOT NULL,
  `room` varchar(50) NOT NULL,
  PRIMARY KEY (`timeSlotId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `timeslot`
--

INSERT INTO `timeslot` (`timeSlotId`, `dayOfWeek`, `startTime`, `endTime`, `room`) VALUES
(1, 'MONDAY', '09:00:00', '10:30:00', 'Room 101'),
(2, 'WEDNESDAY', '10:30:00', '12:00:00', 'Room 203'),
(3, 'FRIDAY', '13:00:00', '14:30:00', 'Room 101'),
(4, 'TUESDAY', '15:30:00', '16:30:00', 'Room 202');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `course`
--
ALTER TABLE `course`
  ADD CONSTRAINT `course_ibfk_1` FOREIGN KEY (`teacherId`) REFERENCES `teacher` (`teacherId`);

--
-- Constraints for table `courseschedule`
--
ALTER TABLE `courseschedule`
  ADD CONSTRAINT `courseschedule_ibfk_1` FOREIGN KEY (`courseId`) REFERENCES `course` (`courseId`),
  ADD CONSTRAINT `courseschedule_ibfk_2` FOREIGN KEY (`timeSlotId`) REFERENCES `timeslot` (`timeSlotId`);

--
-- Constraints for table `enrollment`
--
ALTER TABLE `enrollment`
  ADD CONSTRAINT `enrollment_ibfk_1` FOREIGN KEY (`studentId`) REFERENCES `student` (`studentId`),
  ADD CONSTRAINT `enrollment_ibfk_2` FOREIGN KEY (`courseId`) REFERENCES `course` (`courseId`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
