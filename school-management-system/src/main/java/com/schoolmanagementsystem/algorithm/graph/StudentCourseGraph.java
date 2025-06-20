package com.schoolmanagementsystem.algorithm.graph;

import com.schoolmanagementsystem.model.Student;
import com.schoolmanagementsystem.model.Course;
import com.schoolmanagementsystem.model.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bipartite graph implementation for analyzing student-course relationships
 */
public class StudentCourseGraph {
    private static final Logger logger = LoggerFactory.getLogger(StudentCourseGraph.class);

    private final Map<Integer, Set<Integer>> studentToCourses;
    private final Map<Integer, Set<Integer>> courseToStudents;
    private final Map<Integer, Student> students;
    private final Map<Integer, Course> courses;

    public StudentCourseGraph() {
        this.studentToCourses = new HashMap<>();
        this.courseToStudents = new HashMap<>();
        this.students = new HashMap<>();
        this.courses = new HashMap<>();
    }

    public void addStudent(Student student) {
        students.put(student.getStudentId(), student);
        studentToCourses.putIfAbsent(student.getStudentId(), new HashSet<>());
    }

    public void addCourse(Course course) {
        courses.put(course.getCourseId(), course);
        courseToStudents.putIfAbsent(course.getCourseId(), new HashSet<>());
    }

    public void addEnrollment(Enrollment enrollment) {
        int studentId = enrollment.getStudentId();
        int courseId = enrollment.getCourseId();

        studentToCourses.computeIfAbsent(studentId, k -> new HashSet<>()).add(courseId);
        courseToStudents.computeIfAbsent(courseId, k -> new HashSet<>()).add(studentId);
    }

    public void removeEnrollment(int studentId, int courseId) {
        Set<Integer> studentCourses = studentToCourses.get(studentId);
        if (studentCourses != null) {
            studentCourses.remove(courseId);
        }

        Set<Integer> courseStudents = courseToStudents.get(courseId);
        if (courseStudents != null) {
            courseStudents.remove(studentId);
        }
    }

    public Set<Integer> getStudentCourses(int studentId) {
        return studentToCourses.getOrDefault(studentId, Collections.emptySet());
    }

    public Set<Integer> getCourseStudents(int courseId) {
        return courseToStudents.getOrDefault(courseId, Collections.emptySet());
    }

    /**
     * Find students with similar course loads using Jaccard similarity
     */
    public List<StudentSimilarity> findSimilarStudents(int studentId, double threshold) {
        Set<Integer> studentCourses = getStudentCourses(studentId);
        if (studentCourses.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudentSimilarity> similarities = new ArrayList<>();

        for (int otherStudentId : students.keySet()) {
            if (otherStudentId == studentId) continue;

            Set<Integer> otherStudentCourses = getStudentCourses(otherStudentId);
            if (otherStudentCourses.isEmpty()) continue;

            double similarity = calculateJaccardSimilarity(studentCourses, otherStudentCourses);
            if (similarity >= threshold) {
                similarities.add(new StudentSimilarity(
                    students.get(studentId), 
                    students.get(otherStudentId), 
                    similarity,
                    getCommonCourses(studentCourses, otherStudentCourses)
                ));
            }
        }

        similarities.sort((s1, s2) -> Double.compare(s2.getSimilarity(), s1.getSimilarity()));
        return similarities;
    }

    private double calculateJaccardSimilarity(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private Set<Integer> getCommonCourses(Set<Integer> courses1, Set<Integer> courses2) {
        Set<Integer> common = new HashSet<>(courses1);
        common.retainAll(courses2);
        return common;
    }

    /**
     * Find connected components of students (students connected through common courses)
     */
    public List<Set<Integer>> findStudentCommunities() {
        Set<Integer> visited = new HashSet<>();
        List<Set<Integer>> communities = new ArrayList<>();

        for (int studentId : students.keySet()) {
            if (!visited.contains(studentId)) {
                Set<Integer> community = new HashSet<>();
                dfsStudentCommunity(studentId, visited, community);
                if (community.size() > 1) { // Only include communities with more than one student
                    communities.add(community);
                }
            }
        }

        return communities;
    }

    private void dfsStudentCommunity(int studentId, Set<Integer> visited, Set<Integer> community) {
        visited.add(studentId);
        community.add(studentId);

        Set<Integer> studentCourses = getStudentCourses(studentId);
        for (int courseId : studentCourses) {
            Set<Integer> courseStudents = getCourseStudents(courseId);
            for (int otherStudentId : courseStudents) {
                if (!visited.contains(otherStudentId)) {
                    dfsStudentCommunity(otherStudentId, visited, community);
                }
            }
        }
    }

    /**
     * Get course recommendations for a student based on what similar students are taking
     */
    public List<CourseRecommendation> getCourseRecommendations(int studentId, int maxRecommendations) {
        List<StudentSimilarity> similarStudents = findSimilarStudents(studentId, 0.3); // 30% similarity threshold
        Set<Integer> studentCourses = getStudentCourses(studentId);

        Map<Integer, Double> courseScores = new HashMap<>();

        for (StudentSimilarity similarity : similarStudents) {
            Set<Integer> otherStudentCourses = getStudentCourses(similarity.getStudent2().getStudentId());
            for (int courseId : otherStudentCourses) {
                if (!studentCourses.contains(courseId)) {
                    courseScores.merge(courseId, similarity.getSimilarity(), Double::sum);
                }
            }
        }

        return courseScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(maxRecommendations)
                .map(entry -> new CourseRecommendation(
                    courses.get(entry.getKey()), 
                    entry.getValue(),
                    "Based on similar students' enrollments"
                ))
                .collect(Collectors.toList());
    }

    public void clear() {
        studentToCourses.clear();
        courseToStudents.clear();
        students.clear();
        courses.clear();
    }

    public int getStudentCount() {
        return students.size();
    }

    public int getCourseCount() {
        return courses.size();
    }

    public int getEnrollmentCount() {
        return studentToCourses.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    public static class StudentSimilarity {
        private final Student student1;
        private final Student student2;
        private final double similarity;
        private final Set<Integer> commonCourses;

        public StudentSimilarity(Student student1, Student student2, double similarity, Set<Integer> commonCourses) {
            this.student1 = student1;
            this.student2 = student2;
            this.similarity = similarity;
            this.commonCourses = commonCourses;
        }

        public Student getStudent1() { return student1; }
        public Student getStudent2() { return student2; }
        public double getSimilarity() { return similarity; }
        public Set<Integer> getCommonCourses() { return commonCourses; }

        @Override
        public String toString() {
            return String.format("%s <-> %s (%.2f%% similar, %d common courses)",
                    student1.getFullName(), student2.getFullName(), 
                    similarity * 100, commonCourses.size());
        }
    }

    public static class CourseRecommendation {
        private final Course course;
        private final double score;
        private final String reason;

        public CourseRecommendation(Course course, double score, String reason) {
            this.course = course;
            this.score = score;
            this.reason = reason;
        }

        public Course getCourse() { return course; }
        public double getScore() { return score; }
        public String getReason() { return reason; }

        @Override
        public String toString() {
            return String.format("%s (Score: %.2f) - %s", 
                    course.toString(), score, reason);
        }
    }
}