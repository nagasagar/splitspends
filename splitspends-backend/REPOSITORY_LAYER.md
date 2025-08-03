# 🗄️ SplitSpends Repository Layer

## **📋 Repository Overview**

The repository layer provides comprehensive data access for all SplitSpends entities. Each repository includes specialized methods for business operations, statistics, bulk operations, and performance-optimized queries.

---

## **🔹 Repository Interfaces Created**

### **1. SettleUpRepository** 💰
**Purpose**: Settlement and payment management
**Methods**: 35+ specialized methods

#### **Key Method Categories:**
- **Basic Queries**: Find by group, payer, payee, status
- **User-Specific**: Pending settlements, settlement history, payment tracking
- **Amount-Based**: Total paid/received, pending amounts, amount ranges
- **Business Logic**: Settlement reminders, expired settlements, validation
- **Statistics**: Average amounts, acceptance rates, payment method analytics

#### **Example Business Methods:**
```java
List<SettleUp> findPendingByUser(User user)
BigDecimal getTotalPaidByUser(User user)
List<SettleUp> findSettlementsNeedingReminder(LocalDateTime threshold)
List<SettleUp> findBetweenUsersInGroup(Group group, User user1, User user2)
```

### **2. NotificationRepository** 🔔
**Purpose**: Notification and activity feed management
**Methods**: 30+ specialized methods

#### **Key Method Categories:**
- **Read/Unread Management**: Mark as read, count unread, bulk operations
- **Type-Specific**: Expense, settlement, group notifications
- **Priority-Based**: High priority, urgent notifications
- **Context-Specific**: Group-related, user-triggered notifications
- **Cleanup**: Expired notifications, old notification deletion

#### **Example Business Methods:**
```java
List<Notification> findUnreadByUser(User user)
long countUnreadHighPriorityByUser(User user)
int markAllAsReadForUser(User user)
List<Notification> findPendingActionNotifications(User user)
```

### **3. InvitationRepository** 📨
**Purpose**: Group invitation lifecycle management
**Methods**: 25+ specialized methods

#### **Key Method Categories:**
- **Token Management**: Secure token lookup, validation
- **Status Tracking**: Pending, accepted, declined, expired invitations
- **Email Integration**: Reminder logic, email tracking
- **Expiration Handling**: Auto-expiration, cleanup operations
- **Business Validation**: Duplicate checks, permission validation

#### **Example Business Methods:**
```java
Optional<Invitation> findByInvitationToken(String token)
List<Invitation> findInvitationsNeedingReminder(LocalDateTime threshold)
boolean hasPendingInvitation(String email, Group group)
int markExpiredInvitations()
```

### **4. ActivityLogRepository** 📊
**Purpose**: Comprehensive audit trail and activity tracking
**Methods**: 40+ specialized methods

#### **Key Method Categories:**
- **User Activity**: Login tracking, user-specific actions, interaction history
- **Group Activity**: Group feeds, member activities, collaborative actions
- **Security Audit**: IP tracking, session management, suspicious activity detection
- **Entity Timeline**: Complete history for entities, change tracking
- **Analytics**: Daily stats, user activity patterns, cross-group analysis

#### **Example Business Methods:**
```java
List<ActivityLog> findGroupActivityFeed(Group group, LocalDateTime since)
List<ActivityLog> findRecentLoginsByIp(String ip, LocalDateTime threshold)
List<ActivityLog> findEntityTimeline(EntityType type, Long entityId)
List<Object[]> getDailyActivityCount(LocalDateTime monthAgo)
```

### **5. AttachmentRepository** 📎
**Purpose**: File and receipt management
**Methods**: 35+ specialized methods

#### **Key Method Categories:**
- **File Management**: Storage provider abstraction, file validation
- **Type-Specific**: Images, PDFs, receipts, document categorization
- **Storage Analytics**: Size tracking, storage quotas, provider statistics
- **Lifecycle Management**: Soft deletion, cleanup operations, orphan detection
- **Security**: Checksum validation, duplicate detection, access control

#### **Example Business Methods:**
```java
List<Attachment> findReceiptsByExpense(Expense expense)
Long getTotalStorageByUser(User user)
List<Attachment> findDuplicatesByChecksum(String checksum)
List<Attachment> findNeedingThumbnailGeneration()
```

---

## **🎯 Repository Design Patterns**

### **✅ Consistent Query Patterns**
- **Find by Entity**: `findByEntityOrderByCreatedAtDesc()`
- **Find with Pagination**: Methods include `payable` parameter
- **Find by Status**: Status-specific filtering for all entities
- **Find by Date Range**: Flexible date filtering for all time-sensitive data

### **✅ Business-Focused Methods**
- **User-Centric**: Methods return data relevant to specific users
- **Group-Centric**: Group-specific operations and analytics
- **Permission-Aware**: Queries respect user permissions and access controls
- **Performance-Optimized**: Strategic use of joins and indexing

### **✅ Bulk Operations**
- **Mass Updates**: `@Modifying` queries for bulk status changes
- **Cleanup Operations**: Automated deletion of old/expired data
- **Statistics Generation**: Aggregation queries for reporting
- **Data Migration**: Bulk operations for system maintenance

### **✅ Security & Audit**
- **Soft Deletion**: Preserve data while hiding from normal queries
- **Audit Trail**: Complete tracking of all entity changes
- **IP/Session Tracking**: Security monitoring and analysis
- **Access Validation**: User permission-based filtering

---

## **🔗 Repository Relationships**

```
UserRepository ←→ GroupRepository [membership management]
UserRepository ←→ ExpenseRepository [payment tracking]
UserRepository ←→ SettleUpRepository [settlement management]
UserRepository ←→ NotificationRepository [notification delivery]
UserRepository ←→ ActivityLogRepository [action tracking]
UserRepository ←→ InvitationRepository [invitation management]
UserRepository ←→ AttachmentRepository [file uploads]

GroupRepository ←→ ExpenseRepository [group expenses]
GroupRepository ←→ SettleUpRepository [group settlements]
GroupRepository ←→ InvitationRepository [group invitations]
GroupRepository ←→ ActivityLogRepository [group activities]

ExpenseRepository ←→ ExpenseSplitRepository [expense splitting]
ExpenseRepository ←→ AttachmentRepository [receipts & files]
```

---

## **📈 Advanced Query Features**

### **🔹 Statistical Queries**
```java
// User activity analytics
List<Object[]> countActivitiesByUser()
List<Object[]> getDailyActivityCount(LocalDateTime monthAgo)

// Settlement analytics
Optional<BigDecimal> getAverageSettlementAmountByGroup(Group group)
Double getAcceptanceRateByGroup(Group group)

// Storage analytics
List<Object[]> getStorageStatsByProvider()
List<Object[]> getDailyUploadStats(LocalDateTime monthAgo)
```

### **🔹 Complex Business Logic**
```java
// Settlement workflow
List<SettleUp> findSettlementsNeedingReminder(LocalDateTime threshold)
List<SettleUp> findExpiredPendingSettlements(LocalDateTime threshold)

// Notification intelligence
List<Notification> findPendingActionNotifications(User user)
List<Notification> findActivityFeedNotifications(User user, LocalDateTime since)

// Invitation lifecycle
List<Invitation> findInvitationsNeedingReminder(LocalDateTime threshold)
List<Invitation> findAutoAcceptableInvitations()
```

### **🔹 Data Validation & Integrity**
```java
// Duplicate detection
List<Attachment> findDuplicatesByChecksum(String checksum)
List<Invitation> findDuplicateInvitations(String email, Group group)

// Orphan detection
List<Attachment> findOrphanedAttachments()

// Security validation
boolean existsByInvitationToken(String token)
boolean hasPendingInvitation(String email, Group group)
```

---

## **🚀 Performance Optimizations**

### **✅ Database Indexing**
- All entities have strategic indexes on frequently queried columns
- Composite indexes for complex queries (group_id + date, user_id + status)
- Foreign key indexes for relationship queries

### **✅ Query Optimization**
- **COALESCE Usage**: Null-safe aggregations (`COALESCE(SUM(...), 0)`)
- **Strategic JOINs**: Efficient relationship traversal
- **Pagination Support**: All list methods support `Pageable` parameters
- **Lazy Loading**: FetchType.LAZY for performance

### **✅ Caching Strategy Ready**
- Repository methods designed for cache-friendly patterns
- Immutable query results where possible
- Cache eviction points identified (create/update/delete operations)

---

## **💡 Repository Benefits**

✅ **Complete Data Access** - Every business requirement covered  
✅ **Performance Optimized** - Strategic indexing and efficient queries  
✅ **Security Focused** - Permission-aware queries and audit trails  
✅ **Analytics Ready** - Comprehensive statistics and reporting queries  
✅ **Maintenance Friendly** - Bulk operations and cleanup methods  
✅ **Scalable Design** - Pagination and efficient relationship handling  

**Your repository layer is production-ready and provides a solid foundation for the service layer!** 🎉
