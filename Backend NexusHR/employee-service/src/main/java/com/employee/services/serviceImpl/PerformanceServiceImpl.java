package com.employee.services.serviceImpl;

import com.employee.domain.entity.Employee;
import com.employee.domain.entity.Goal;
import com.employee.domain.entity.PerformanceReview;
import com.employee.domain.enums.EmployeeStatus;
import com.employee.domain.enums.GoalStatus;
import com.employee.dtos.request.GoalRequestDto;
import com.employee.dtos.request.ReviewRequestDto;
import com.employee.dtos.response.GoalResponseDto;
import com.employee.dtos.response.ReviewResponseDto;
import com.employee.dtos.response.ScoreCard;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.EmployeeRepository;
import com.employee.repositories.GoalRepository;
import com.employee.repositories.PerformanceReviewRepository;
import com.employee.services.service.PerformanceService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


@Service
public class PerformanceServiceImpl implements PerformanceService {

    private static final ZoneId OFFICE_ZONE = ZoneId.of("Asia/Kolkata");

    private final GoalRepository goalRepository;
    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;

    public PerformanceServiceImpl(GoalRepository goalRepository,
                                  PerformanceReviewRepository reviewRepository,
                                  EmployeeRepository employeeRepository) {
        this.goalRepository = goalRepository;
        this.reviewRepository = reviewRepository;
        this.employeeRepository = employeeRepository;
    }


    @Override
    @Transactional
    public GoalResponseDto createGoal(GoalRequestDto request, Authentication authentication) {
        Employee caller = findCaller(authentication);

        Employee owner = request.getEmployeeId() == null
                ? caller
                : findEmployee(request.getEmployeeId());

        if (!owner.getId().equals(caller.getId()) && !canManage(owner, authentication)) {
            throw new AccessDeniedException("You can only create goals for yourself or your team");
        }
        if (owner.getStatus() == EmployeeStatus.EXITED) {
            throw new IllegalStateException("You can't create a goal for an exited employee");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        if (request.getDueDate().isBefore(LocalDate.now(OFFICE_ZONE))) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }

        Goal goal = new Goal();
        goal.setEmployee(owner);
        goal.setTitle(request.getTitle().trim());
        goal.setDueDate(request.getDueDate());
        goal.setProgress(request.getProgress() == null ? 0 : request.getProgress());
        goal.setStatus(request.getStatus() == null ? GoalStatus.ON_TRACK : request.getStatus());

        applyDoneRule(goal);
        return toGoalResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponseDto updateGoal(Long goalId, GoalRequestDto request, Authentication authentication) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id " + goalId));

        boolean isOwner = goal.getEmployee().getEmail().equalsIgnoreCase(authentication.getName());
        if (!isOwner && !canManage(goal.getEmployee(), authentication)) {
            throw new AccessDeniedException("You can only update your own goals or your team's");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            goal.setTitle(request.getTitle().trim());
        }
        if (request.getProgress() != null) {
            goal.setProgress(request.getProgress());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            goal.setDueDate(request.getDueDate());
        }

        applyDoneRule(goal);
        return toGoalResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getMyGoals(Authentication authentication) {
        Employee me = findCaller(authentication);
        return goalRepository.findByEmployeeIdOrderByDueDateAsc(me.getId()).stream()
                .map(this::toGoalResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getEmployeeGoals(Long employeeId, Authentication authentication) {
        Employee employee = findEmployee(employeeId);
        checkCanView(employee, authentication);

        return goalRepository.findByEmployeeIdOrderByDueDateAsc(employeeId).stream()
                .map(this::toGoalResponse)
                .toList();
    }

    private void applyDoneRule(Goal goal) {
        if (goal.getProgress() == 100) {
            goal.setStatus(GoalStatus.DONE);
        } else if (goal.getStatus() == GoalStatus.DONE) {
            throw new IllegalStateException("A goal can only be DONE at 100% progress");
        }
    }

    // ===================================================================== reviews

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto request, Authentication authentication) {

        Employee reviewer = findCaller(authentication);
        Employee employee = findEmployee(request.getEmployeeId());

        if (employee.getStatus() == EmployeeStatus.EXITED) {
            throw new IllegalStateException("Cannot review an exited employee");
        }

        boolean isSelf = reviewer.getId().equals(employee.getId());

        switch (request.getType()) {
            case SELF -> {
                if (!isSelf) {
                    throw new AccessDeniedException("A SELF review can only be written about yourself");
                }
            }
            case MANAGER -> {
                boolean isTheirManager = employee.getManager() != null
                        && employee.getManager().getId().equals(reviewer.getId());
                if (!isTheirManager) {
                    throw new AccessDeniedException("Only this employee's manager can write a MANAGER review");
                }
            }
            case PEER -> {
                boolean sameDepartment = employee.getDepartment() != null && reviewer.getDepartment() != null
                        && employee.getDepartment().getId().equals(reviewer.getDepartment().getId());
                if (isSelf || !sameDepartment) {
                    throw new AccessDeniedException("A PEER review must be about a colleague in your department");
                }
            }
        }

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setType(request.getType());
        review.setRating(request.getRating());
        review.setComments(request.getComments() == null ? null : request.getComments().trim());

        return toReviewResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getEmployeeReviews(Long employeeId, Authentication authentication) {
        Employee employee = findEmployee(employeeId);
        checkCanView(employee, authentication);

        return reviewRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toReviewResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public ScoreCard getEmployeeScorecard(Long employeeId, Authentication authentication) {
        Employee employee = findEmployee(employeeId);
        checkCanView(employee, authentication);

        List<Goal> goals = goalRepository.findByEmployeeIdOrderByDueDateAsc(employeeId);
        List<PerformanceReview> reviews = reviewRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);

        int completedGoals = (int) goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.DONE)
                .count();

        Double goalCompletion = goals.isEmpty() ? null
                : round1(goals.stream().mapToInt(Goal::getProgress).average().orElse(0));

        Double averageRating = reviews.isEmpty() ? null
                : round1(reviews.stream().mapToInt(PerformanceReview::getRating).average().orElse(0));

        return new ScoreCard(
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                goals.size(),
                completedGoals,
                goalCompletion,
                reviews.size(),
                averageRating);
    }

    private boolean canManage(Employee employee, Authentication authentication) {
        boolean isHrOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = employee.getManager() != null
                && employee.getManager().getEmail().equalsIgnoreCase(authentication.getName());
        return isHrOrAdmin || isManager;
    }

    private void checkCanView(Employee employee, Authentication authentication) {
        boolean isOwner = employee.getEmail().equalsIgnoreCase(authentication.getName());
        if (!isOwner && !canManage(employee, authentication)) {
            throw new AccessDeniedException("You can only view your own performance or your team's");
        }
    }


    private Employee findCaller(Authentication authentication) {
        return employeeRepository.findByEmail(authentication.getName().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No employee profile found for " + authentication.getName()));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    private static Double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private GoalResponseDto toGoalResponse(Goal goal) {
        GoalResponseDto response = new GoalResponseDto();
        response.setId(goal.getId());
        response.setEmployeeId(goal.getEmployee().getId());
        response.setEmployeeName(goal.getEmployee().getFirstName() + " " + goal.getEmployee().getLastName());
        response.setTitle(goal.getTitle());
        response.setProgress(goal.getProgress());
        response.setStatus(goal.getStatus());
        response.setDueDate(goal.getDueDate());
        return response;
    }

    private ReviewResponseDto toReviewResponse(PerformanceReview review) {
        ReviewResponseDto response = new ReviewResponseDto();
        response.setId(review.getId());
        response.setEmployeeId(review.getEmployee().getId());
        response.setEmployeeName(review.getEmployee().getFirstName() + " " + review.getEmployee().getLastName());
        response.setReviewerId(review.getReviewer().getId());
        response.setReviewerName(review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName());
        response.setType(review.getType());
        response.setRating(review.getRating());
        response.setComments(review.getComments());
        response.setSubmittedAt(review.getCreatedAt());
        return response;
    }
}