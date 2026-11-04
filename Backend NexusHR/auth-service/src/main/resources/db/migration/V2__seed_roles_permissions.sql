-- ---------- Roles ----------
INSERT INTO roles (name, description, created_at, updated_at, created_by, version) VALUES
    ('ADMIN',    'Full system access',                        NOW(), NOW(), 'system', 0),
    ('HR',       'HR operations: employees, leave, payroll',  NOW(), NOW(), 'system', 0),
    ('MANAGER',  'Team approvals and team insights',          NOW(), NOW(), 'system', 0),
    ('EMPLOYEE', 'Self-service access',                       NOW(), NOW(), 'system', 0);

-- ---------- Permissions ----------
INSERT INTO permission (code, description, created_at, updated_at, created_by, version) VALUES
    ('USER_MANAGE',        'Create users, assign roles, change account status', NOW(), NOW(), 'system', 0),
    ('AUDIT_READ',         'Read audit logs',                                   NOW(), NOW(), 'system', 0),
    ('EMPLOYEE_READ_ALL',  'View all employee records',                         NOW(), NOW(), 'system', 0),
    ('EMPLOYEE_WRITE',     'Create and update employee records',                NOW(), NOW(), 'system', 0),
    ('TEAM_READ',          'View records of own team',                          NOW(), NOW(), 'system', 0),
    ('LEAVE_APPROVE',      'Approve or reject leave requests',                  NOW(), NOW(), 'system', 0),
    ('PAYROLL_PREPARE',    'Create, calculate and validate payroll runs',       NOW(), NOW(), 'system', 0),
    ('PAYROLL_APPROVE',    'Approve and disburse payroll runs',                 NOW(), NOW(), 'system', 0),
    ('PERFORMANCE_MANAGE', 'Manage review cycles and calibration',              NOW(), NOW(), 'system', 0),
    ('INSIGHTS_READ',      'View AI workforce insights',                        NOW(), NOW(), 'system', 0),
    ('SELF_SERVICE',       'Own profile, attendance, leave and payslips',       NOW(), NOW(), 'system', 0);

-- ---------- Role → permissions ----------
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permission p ON
       r.name = 'ADMIN'
    OR (r.name = 'HR' AND p.code IN ('EMPLOYEE_READ_ALL', 'EMPLOYEE_WRITE', 'LEAVE_APPROVE',
                                     'PAYROLL_PREPARE', 'PERFORMANCE_MANAGE', 'INSIGHTS_READ', 'SELF_SERVICE'))
    OR (r.name = 'MANAGER' AND p.code IN ('TEAM_READ', 'LEAVE_APPROVE', 'INSIGHTS_READ', 'SELF_SERVICE'))
    OR (r.name = 'EMPLOYEE' AND p.code IN ('SELF_SERVICE'));