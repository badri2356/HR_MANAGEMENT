-- One row per employee per skill
CREATE TABLE employee_skills (
    employee_id BIGINT      NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
    skill       VARCHAR(50) NOT NULL,
    PRIMARY KEY (employee_id, skill)          -- the same skill can't be added twice for one person
);