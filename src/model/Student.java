package model;

public class Student {
    private String id;
    private String name;
    private double score;
    private String rank;

    public Student(String id, String name, double score) {
        this.id = id;
        this.name = name;
        this.score = score;
        calculateRank();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
        calculateRank();
    }

    public String getRank() {
        return rank;
    }

    private void calculateRank() {
        if (score < 5.0) {
            rank = "Fail";
        } else if (score < 6.5) {
            rank = "Medium";
        } else if (score < 7.5) {
            rank = "Good";
        } else if (score < 9.0) {
            rank = "Very Good";
        } else {
            rank = "Excellent";
        }
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Score: " + score + ", Rank: " + rank;
    }
}

