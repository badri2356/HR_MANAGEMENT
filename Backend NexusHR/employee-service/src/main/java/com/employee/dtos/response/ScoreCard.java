package com.employee.dtos.response;

public record ScoreCard(

        Long employeeId,

        String employeeName,

        int totalGoals,            // how many goals the employee has

        int completedGoals,        // goals with status done

        Double goalCompletion,     // average progress of all goals. null when there are no goals

        int totalReviews,          // how many reviews the employee has received

        Double averageRating       // average of all ratings. null when there are no reviews yet
) {
}