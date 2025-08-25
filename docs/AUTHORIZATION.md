
# SplitSpends Authorization Rules & Permissions

## Overview
This document defines comprehensive authorization rules for the SplitSpends application, specifying what information each user can see and what actions they can perform.

## Core Authorization Principles

### 1. **User Privacy**
- Users can only see their own private information (settings, balances, personal expenses)
- Users can see basic information about other users only if they share groups
- No user can access another user's private data without explicit sharing

### 2. **Group-Based Access**
- All expense and financial data is group-scoped
- Users can only see data from groups they are members of
- Group membership is the primary access control mechanism

### 3. **Role-Based Permissions**
- **Group Members**: Basic access to group data
- **Group Admins**: Enhanced management capabilities
- **Group Creator**: Special privileges for group lifecycle

### 4. **Data Visibility Levels**
- **Public**: Searchable by anyone (limited group info only)
- **Group Members**: Full access to group data
- **Self Only**: Personal user data and settings

---

## Detailed Authorization Rules

### 👤 USER DATA ACCESS

#### Profile Information
| Data Type | Own Profile | Group Members | Non-Group Users |
|-----------|------------|---------------|----------------|
| Basic Info (name, email) | ✅ Full Access | ✅ View Only | ❌ No Access |
| Profile Picture | ✅ Full Access | ✅ View Only | ❌ No Access |
| Phone Number | ✅ Full Access | ❌ No Access | ❌ No Access |
| Personal Settings | ✅ Full Access | ❌ No Access | ❌ No Access |
| Account Status | ✅ Full Access | ❌ No Access | ❌ No Access |
| Joined Date | ✅ Full Access | ✅ View Only | ❌ No Access |

#### Financial & Activity Data
| Data Type | Own Data | Other Users |
|-----------|----------|-------------|
| Personal Expenses | ✅ Full Access | ❌ No Access |
| Personal Balances | ✅ Full Access | ❌ No Access |
| Groups List | ✅ Full Access | ❌ No Access |
| Activity History | ✅ Full Access | ❌ No Access |
| Notifications | ✅ Full Access | ❌ No Access |

### 🏠 GROUP DATA ACCESS

#### Basic Group Information
| User Type | Group Name | Description | Image | Created Date | Member Count |
|-----------|------------|-------------|-------|--------------|--------------|
| **Public Groups** | | | | | |
| - Anyone | ✅ View | ✅ View | ✅ View | ✅ View | ✅ View |
| **Private Groups** | | | | | |
| - Group Members | ✅ View | ✅ View | ✅ View | ✅ View | ✅ View |
| - Non-Members | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access | ❌ No Access |

#### Group Management Data
| Data Type | Group Members | Group Admins | Non-Members |
|-----------|---------------|--------------|-------------|
| Member List | ✅ View | ✅ View + Manage | ❌ No Access |
| Admin List | ✅ View | ✅ View + Manage | ❌ No Access |
| Group Settings | ✅ View Basic | ✅ View + Edit All | ❌ No Access |
| Invitation Settings | ❌ No Access | ✅ View + Edit | ❌ No Access |
| Group Statistics | ✅ View Basic | ✅ View Detailed | ❌ No Access |

#### Financial Group Data
| Data Type | Group Members | Group Admins | Non-Members |
|-----------|---------------|--------------|-------------|
| Group Expenses | ✅ View All | ✅ View + Manage All | ❌ No Access |
| Group Balances | ✅ View All | ✅ View All | ❌ No Access |
| Settlement Requests | ✅ View All | ✅ View + Manage | ❌ No Access |
| Payment History | ✅ View All | ✅ View + Export | ❌ No Access |

### 💰 EXPENSE DATA ACCESS

#### Expense Visibility
| Expense Type | Group Members | Expense Participants | Non-Group Members |
|--------------|---------------|---------------------|-------------------|
| Group Expenses | ✅ View All Details | ✅ View All Details | ❌ No Access |
| Personal Involvement | ✅ View Splits & Balances | ✅ View Splits & Balances | ❌ No Access |
| Expense Attachments | ✅ View All | ✅ View All | ❌ No Access |

#### Expense Management
| Action | Expense Creator | Group Admins | Other Group Members |
|--------|----------------|--------------|-------------------|
| Create Expense | ✅ Yes | ✅ Yes | ✅ Yes |
| Edit Own Expense | ✅ Yes (time limited) | ✅ Yes | ❌ No |
| Edit Others' Expense | ❌ No | ✅ Yes | ❌ No |
| Delete Expense | ✅ Yes (time limited) | ✅ Yes | ❌ No |
| Add Attachments | ✅ Yes | ✅ Yes | ✅ Yes |

---

## 🔐 MODIFICATION PERMISSIONS

### User Profile Management
| Action | Self | Other Users |
|--------|------|-------------|
| Edit Basic Info | ✅ Yes | ❌ No |
| Change Password | ✅ Yes | ❌ No |
| Update Settings | ✅ Yes | ❌ No |
| Delete Account | ✅ Yes | ❌ No |

### Group Management Permissions
| Action | Group Creator | Group Admins | Group Members | Non-Members |
|--------|---------------|--------------|---------------|-------------|
| **Basic Group Management** |
| Edit Group Info | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| Upload Group Image | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| Change Group Settings | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| **Member Management** |
| Add Members | ✅ Yes | ✅ Yes | 🟡 Policy Dependent | ❌ No |
| Remove Members | ✅ Yes | ✅ Yes | 🟡 Self Only | ❌ No |
| Promote to Admin | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| Demote Admin | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| **Group Lifecycle** |
| Archive Group | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| Delete Group | ✅ Yes | 🟡 If Creator Left | ❌ No | ❌ No |
| Leave Group | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |

### Invitation Policies
| Policy | Who Can Invite |
|--------|----------------|
| **ADMIN_ONLY** | Only group admins can send invitations |
| **ALL_MEMBERS** | All group members can send invitations |
| **CREATOR_ONLY** | Only the group creator can send invitations |

---

## 🔍 SEARCH & DISCOVERY

### Public Group Search
- **Anyone**: Can search and view basic info of public groups
- **Searchable Fields**: Group name, description
- **Visible Info**: Name, description, image, member count, created date
- **Hidden Info**: Member list, expenses, balances

### User Search
- **Group Members**: Can search for users within their groups
- **Visible Fields**: Name, email, profile picture
- **Hidden Fields**: Phone, settings, financial data

---

## 📊 REPORTING & ANALYTICS

### Personal Reports
| Report Type | Own Data | Other Users' Data |
|-------------|----------|-------------------|
| Personal Expense Summary | ✅ Yes | ❌ No |
| Personal Balance Report | ✅ Yes | ❌ No |
| Personal Activity Log | ✅ Yes | ❌ No |

### Group Reports
| Report Type | Group Members | Group Admins | Non-Members |
|-------------|---------------|--------------|-------------|
| Group Expense Summary | ✅ View | ✅ View + Export | ❌ No |
| Group Balance Report | ✅ View | ✅ View + Export | ❌ No |
| Member Activity Report | ❌ No | ✅ View | ❌ No |
| Group Analytics | 🟡 Basic | ✅ Detailed | ❌ No |

---

## 🚨 SECURITY CONSIDERATIONS

### Time-Based Restrictions
- **Expense Modifications**: Limited to 24 hours after creation (configurable)
- **Group Leaving**: Cannot leave if you have unsettled balances
- **Admin Removal**: Cannot remove last admin from group

### Data Protection
- **Email Addresses**: Only visible to group members
- **Phone Numbers**: Never shared with other users
- **Financial Data**: Strictly group-scoped
- **Personal Settings**: Never shared

### Audit Trail
- All permission checks are logged
- Failed authorization attempts trigger security alerts
- Group membership changes are tracked
- Admin actions are audited

---

## 🛠️ IMPLEMENTATION NOTES

### Method-Level Security
```java
@PreAuthorize("@authorizationService.canViewGroup(#groupId)")
public GroupResponse getGroup(Long groupId) { ... }

@PreAuthorize("@authorizationService.canModifyGroup(#groupId)")
public GroupResponse updateGroup(Long groupId, ...) { ... }
```

### Service-Level Validation
```java
// Always validate current user context
User currentUser = authorizationService.getCurrentUser();

// Check specific permissions
authorizationService.requireGroupMembership(groupId);
authorizationService.requireGroupAdmin(groupId);
```

### Exception Handling
- **SecurityException**: Thrown for authorization failures
- **AccessDeniedException**: Thrown for insufficient permissions
- **IllegalArgumentException**: Thrown for invalid resource access

---

## 📋 PERMISSION MATRIX SUMMARY

| Resource | Self | Group Members | Group Admins | Non-Group | Public |
|----------|------|---------------|--------------|-----------|--------|
| **User Profile** | Full | Basic | Basic | None | None |
| **User Financial** | Full | None | None | None | None |
| **Group Basic** | Full | Full | Full | None | Limited |
| **Group Management** | N/A | View | Full | None | None |
| **Group Financial** | N/A | Full | Full | None | None |
| **Expenses** | Own | Group | Group | None | None |

### Legend
- **Full**: Complete read/write access
- **Basic**: Limited read access
- **Limited**: Very restricted read access  
- **View**: Read-only access
- **None**: No access
- **Own**: Only user's own data
- **Group**: Only data from user's groups

---

## 👑 SUPER ADMIN AUTHORIZATION

### 🤔 **Why Super Admin is Essential**

For a SplitSpends application, a **Super Admin role is CRITICAL** for:

#### **1. Customer Support & Troubleshooting**
- **Account Recovery**: Help users who lost access to their accounts
- **Dispute Resolution**: Mediate conflicts between group members over expenses
- **Technical Issues**: Debug payment processing, data corruption, sync issues
- **Password Resets**: Assist users locked out of their accounts

#### **2. Platform Management**
- **User Moderation**: Suspend abusive or spam accounts
- **Content Moderation**: Remove inappropriate group names or descriptions
- **System Monitoring**: View platform-wide usage statistics and health metrics
- **Data Integrity**: Fix corrupted data, merge duplicate accounts

#### **3. Legal & Compliance**
- **GDPR Requests**: Export or delete user data for privacy compliance
- **Legal Investigations**: Provide data for law enforcement (with proper warrants)
- **Audit Requirements**: Track and log all administrative actions
- **Terms of Service**: Enforce platform rules and policies

#### **4. Business Operations**
- **Analytics**: Platform growth, user engagement, feature usage
- **Support Metrics**: Response times, common issues, user satisfaction
- **Financial Oversight**: Transaction volumes, payment processing issues
- **Security Monitoring**: Detect suspicious activity, fraud prevention

---

## 🛡️ **Super Admin Security Model**

### **System Role Hierarchy:**
```
USER (default)
    ↓
SUPPORT (customer service agents)
    ↓
ADMIN (system administrators)
    ↓
SUPER_ADMIN (platform owners)
```

### **Super Admin Permission Matrix:**

| Operation | USER | SUPPORT | ADMIN | SUPER_ADMIN |
|-----------|------|---------|-------|-------------|
| **View own data** | ✅ | ✅ | ✅ | ✅ |
| **View any user profile** | ❌ | ✅ | ✅ | ✅ |
| **View user financial data** | ❌ | 🟡 Limited | ✅ | ✅ |
| **Suspend user accounts** | ❌ | ❌ | ✅ | ✅ |
| **Access all groups** | ❌ | ✅ | ✅ | ✅ |
| **Moderate content** | ❌ | ✅ | ✅ | ✅ |
| **View platform statistics** | ❌ | ❌ | ✅ | ✅ |
| **GDPR data export/deletion** | ❌ | ❌ | ❌ | ✅ |
| **System configuration** | ❌ | ❌ | ✅ | ✅ |
| **Grant admin roles** | ❌ | ❌ | ❌ | ✅ |

---

## 🔒 **Super Admin Access Controls**

### **What Super Admin CAN Access:**
- ✅ **User Profiles**: View all user basic information and account status
- ✅ **User Management**: Suspend/reactivate accounts, reset passwords
- ✅ **Group Information**: View all groups, member lists, basic settings
- ✅ **Platform Statistics**: User counts, group counts, activity metrics
- ✅ **Content Moderation**: Remove inappropriate content and handle abuse
- ✅ **GDPR Compliance**: Export/delete user data for legal requirements
- ✅ **Audit Logs**: View all administrative actions and system activity

### **What Super Admin CANNOT Access:**
- ❌ **Detailed Financial Amounts**: Expense amounts are filtered/hidden
- ❌ **User Passwords**: Only password reset capability, no password viewing
- ❌ **Financial Transactions**: Cannot modify existing transactions
- ❌ **Direct Database Access**: All access goes through secured application layer
- ❌ **Unlogged Actions**: Every super admin action is audited

### **Super Admin Data Filtering:**
```java
// Financial data is filtered for super admin view
public class SuperAdminFinancialFilter {
    public ExpenseResponse filterForSuperAdmin(Expense expense) {
        return ExpenseResponse.builder()
            .id(expense.getId())
            .description(expense.getDescription())
            .category(expense.getCategory())
            .date(expense.getDate())
            .amount("[REDACTED]") // Hide actual amounts
            .paidBy(expense.getPaidBy().getName())
            .groupName(expense.getGroup().getName())
            .build();
    }
}
```

---

## 📊 **Super Admin Endpoints**

### **User Management**
```http
GET /api/super-admin/users?page=0&size=20&search=john
PUT /api/super-admin/users/{userId}/suspend
PUT /api/super-admin/users/{userId}/reactivate  
POST /api/super-admin/users/{userId}/reset-password
```

### **Group Management**
```http
GET /api/super-admin/groups?page=0&size=20&search=travel
GET /api/super-admin/groups/{groupId}
```

### **Platform Analytics**
```http
GET /api/super-admin/platform/stats
```

### **GDPR Compliance**
```http
POST /api/super-admin/users/{userId}/export-data
DELETE /api/super-admin/users/{userId}/delete-data
```

---

## 🔐 **Super Admin Security Implementation**

### **1. Method-Level Security**
```java
@PreAuthorize("@authorizationService.isSuperAdmin()")
@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {
    
    @GetMapping("/users")
    @PreAuthorize("@authorizationService.isSuperAdmin()")
    public ResponseEntity<Page<UserResponse>> getAllUsers(...) {
        // Super admin only endpoint
    }
}
```

### **2. Authorization Service Integration**
```java
@Service
public class AuthorizationService {
    
    public boolean isSuperAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && 
               User.SystemRole.SUPER_ADMIN.equals(currentUser.getSystemRole());
    }
    
    public boolean hasAdminPrivileges() {
        User currentUser = getCurrentUser();
        return currentUser != null && 
               currentUser.hasAdminPrivileges();
    }
}
```

### **3. Comprehensive Audit Logging**
```java
@Service
public class SuperAdminAuditService {
    
    public void logAdminAction(String action, Long targetId, String details) {
        User admin = authorizationService.getCurrentUser();
        
        AuditLog log = AuditLog.builder()
            .adminUser(admin)
            .action(action)
            .targetType("USER") // or "GROUP", "PLATFORM"
            .targetId(targetId)
            .details(details)
            .ipAddress(getCurrentClientIP())
            .timestamp(LocalDateTime.now())
            .build();
            
        auditRepository.save(log);
    }
}
```

### **4. User Entity Role System**
```java
@Entity
public class User {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "system_role")
    private SystemRole systemRole = SystemRole.USER;
    
    public enum SystemRole {
        USER,           // Regular users
        SUPPORT,        // Customer support agents  
        ADMIN,          // System administrators
        SUPER_ADMIN     // Platform owners
    }
    
    public boolean isSuperAdmin() {
        return SystemRole.SUPER_ADMIN.equals(this.systemRole);
    }
    
    public boolean hasAdminPrivileges() {
        return systemRole != null && 
               (systemRole.equals(SystemRole.ADMIN) || 
                systemRole.equals(SystemRole.SUPER_ADMIN));
    }
}
```

---

## 🚨 **Super Admin Security Safeguards**

### **Critical Security Requirements:**
1. **🔍 Audit Logging**: Every super admin action MUST be logged
2. **🛡️ Limited Financial Access**: Actual expense amounts are hidden
3. **⏱️ Session Timeouts**: Super admin sessions expire quickly (5 minutes)
4. **🔐 Multi-Factor Authentication**: MFA required for super admin accounts
5. **📍 IP Restrictions**: Limit super admin access to specific IP addresses

### **What Super Admin Should NEVER Have:**
- ❌ **Unlogged Access**: Every action must be audited
- ❌ **Unlimited Financial Access**: Detailed amounts must be protected
- ❌ **Permanent Sessions**: Sessions should timeout quickly
- ❌ **Single-Factor Auth**: MFA should be mandatory
- ❌ **Direct Database Access**: All access through application layer

### **Security Implementation Checklist:**
- [x] **Role-Based Access Control**: Only SUPER_ADMIN users can access endpoints
- [x] **Method-Level Security**: All endpoints protected with @PreAuthorize
- [x] **Service Layer Integration**: Super admin methods in UserService/GroupService
- [x] **Repository Extensions**: Added pagination and search capabilities
- [ ] **Audit Dashboard**: UI for viewing admin activity logs
- [ ] **MFA Integration**: Multi-factor authentication for super admin accounts
- [ ] **IP Restrictions**: Whitelist-based access control
- [ ] **Session Management**: Short-lived sessions for admin accounts

---

## 🎯 **Super Admin Usage Guidelines**

### **1. Grant Super Admin Role**
```sql
-- Update user to super admin role
UPDATE users 
SET system_role = 'SUPER_ADMIN' 
WHERE email = 'admin@splitspends.com';
```

### **2. Access Super Admin Functions**
```bash
# Authenticate as super admin
POST /api/auth/login
{
  "email": "admin@splitspends.com", 
  "password": "secure_admin_password"
}

# Use JWT token for super admin endpoints
Authorization: Bearer <jwt_token>
GET /api/super-admin/platform/stats
```

### **3. Monitor Admin Activity**
```java
// All super admin actions are automatically logged
// Query audit logs for compliance and security monitoring
SELECT * FROM audit_logs 
WHERE admin_user_id = ? 
AND created_at >= ? 
ORDER BY created_at DESC;
```

---

## 📋 **Implementation Status**

### **✅ COMPLETED FEATURES**
- **System Role Enum**: Added USER, SUPPORT, ADMIN, SUPER_ADMIN hierarchy
- **User Entity Updates**: Role helper methods and system role field
- **Authorization Service**: Super admin access methods and security checks
- **Super Admin Controller**: Complete CRUD operations for users/groups/platform
- **Service Layer**: Enhanced UserService and GroupService with admin methods
- **Repository Layer**: Added pagination and search for admin operations
- **Security Integration**: Method-level security with @PreAuthorize annotations

### **🔄 RECOMMENDED ENHANCEMENTS**
- **Audit Dashboard**: Web UI for viewing administrative activity logs
- **MFA Integration**: Multi-factor authentication for super admin accounts
- **Alert System**: Notifications for suspicious or high-risk admin operations
- **Compliance Reports**: Automated GDPR and audit compliance reporting
- **Session Management**: Enhanced session controls for administrative accounts

---

## 🏆 **Super Admin Benefits**

✅ **Platform Oversight**: Complete visibility into system health and usage
✅ **User Support**: Ability to help users with account and technical issues  
✅ **Legal Compliance**: GDPR and privacy law compliance capabilities
✅ **Security Response**: Tools to investigate and respond to security incidents
✅ **Business Intelligence**: Access to platform analytics and operational metrics
✅ **Content Moderation**: Ability to maintain platform quality and safety
✅ **Audit Trail**: Complete logging for security and compliance requirements

**🚀 Your SplitSpends platform now has enterprise-grade administrative capabilities with proper security boundaries and audit controls!**

````
