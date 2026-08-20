package com.devtrack.util;

import java.util.Arrays;
import java.util.Set;

public final class ResumeKeywordData {

    private ResumeKeywordData() {}

    public static final Set<String> STOPWORDS = Set.of(
        "a", "an", "the", "and", "or", "but", "if", "then", "so", "for", "to", "of", "in", "on",
        "at", "by", "with", "as", "is", "are", "was", "were", "be", "been", "being", "this", "that",
        "these", "those", "it", "its", "we", "you", "your", "our", "will", "shall", "can", "should",
        "must", "may", "might", "have", "has", "had", "do", "does", "did", "not", "no", "into",
        "about", "than", "such", "also", "etc", "including", "using", "who", "which",
        "their", "they", "them", "he", "she", "his", "her", "i", "us", "from", "up", "out", "over",
        "under", "again", "further", "all", "any", "both", "each", "few", "more", "most", "other",
        "some", "own", "same", "too", "very", "just", "one", "two", "three", "years", "year",
        "experience", "strong", "good", "excellent", "ability", "skills", "knowledge", "work",
        "working", "team", "role", "job", "candidate", "responsibilities", "requirements"
    );

    public static final Set<String> TECH_DICTIONARY = Set.of(
        "java", "spring", "springboot", "spring-boot", "hibernate", "jpa", "rest", "restful",
        "microservices", "microservice", "docker", "kubernetes", "k8s", "postgresql", "postgres",
        "mysql", "mongodb", "redis", "kafka", "rabbitmq", "react", "reactjs", "angular", "vue",
        "javascript", "typescript", "node", "nodejs", "python", "django", "flask", "aws", "azure",
        "gcp", "cicd", "ci/cd", "jenkins", "git", "github", "gitlab", "jwt", "oauth", "oauth2",
        "security", "junit", "mockito", "testing", "swagger", "openapi", "sql", "nosql", "graphql",
        "html", "css", "sass", "webpack", "maven", "gradle", "linux", "terraform", "ansible",
        "elasticsearch", "grpc", "websocket", "agile", "scrum", "devops", "tdd", "bdd", "rbac",
        "authentication", "authorization", "dto", "orm", "api", "apis", "backend", "frontend",
        "fullstack", "full-stack", "database", "caching", "loadbalancing", "load-balancing"
    );

    public static final String[][] WEAK_PHRASES = {
        {"responsible for", "Passive, low-impact opener. Replace with an action verb (Built, Engineered, Designed, Led)."},
        {"worked on", "Vague - doesn't say what you actually did or built."},
        {"helped with", "Undersells your contribution. State your specific role directly."},
        {"duties included", "Reads like a job posting, not an achievement."},
        {"assisted in", "Undersells your contribution and suggests a minor role."},
        {"was involved in", "Passive and vague - name your actual contribution."},
        {"participated in", "Passive - doesn't show ownership."},
        {"in charge of", "Reads like a title, not an accomplishment."},
        {"tasked with", "Frames the work as an assignment rather than an achievement."},
        {"various technologies", "Non-specific - name the actual tools/languages/frameworks."},
        {"basic understanding", "Undersells your skill level - recruiters read this as a weakness."},
        {"simple project", "Undersells the work - describe the problem it actually solved."},
        {"as required", "Filler phrase with no information content."},
        {"and other duties", "Filler - remove or replace with a concrete responsibility."},
        {"hard working", "Generic self-description with no evidence."},
        {"team player", "Cliche - show collaboration through a concrete example instead."}
    };

    public static final Set<String> STRONG_ACTION_VERBS = Set.of(
        "built", "engineered", "designed", "developed", "implemented", "architected", "led",
        "optimized", "automated", "reduced", "increased", "improved", "launched", "deployed",
        "created", "refactored", "migrated", "scaled", "integrated", "delivered", "streamlined",
        "spearheaded", "orchestrated", "containerized", "secured", "resolved", "accelerated"
    );

    public static String[] splitSentences(String text) {
        return Arrays.stream(text.split("(?<=[.!?\\n])\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
