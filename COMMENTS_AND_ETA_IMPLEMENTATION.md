# Comments and ETA Update Implementation

## Overview
This document describes the implementation of the comments system and ETA update functionality with comments.

## Database Changes

### New Table: FLOWAI_REQUEST_COMMENTS
A separate table was created for comments (not a column in FLOWAI_REQUESTS) to support:
- Multiple comments per ticket
- ETA change history tracking
- Comment metadata (who, when, why)

**Table Structure:**
```sql
CREATE TABLE FLOWAI_REQUEST_COMMENTS (
    COMMENT_ID NUMBER PRIMARY KEY,
    REQUEST_ID NUMBER NOT NULL,
    COMMENT_TEXT CLOB NOT NULL,
    COMMENTED_BY VARCHAR2(50) NOT NULL,
    COMMENTED_AT TIMESTAMP NOT NULL,
    IS_ETA_CHANGE NUMBER(1) DEFAULT 0 NOT NULL,
    OLD_ETA TIMESTAMP,
    NEW_ETA TIMESTAMP,
    CHANGE_REASON VARCHAR2(500),
    ACTIVE NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT FK_COMMENT_REQUEST FOREIGN KEY (REQUEST_ID) REFERENCES FLOWAI_REQUESTS(REQUEST_ID) ON DELETE CASCADE,
    CONSTRAINT FK_COMMENT_USER FOREIGN KEY (COMMENTED_BY) REFERENCES FLOWAI_USERS(NTID)
);
```

## Backend Implementation

### 1. Entity Classes
- **RequestComment.java**: Entity for comments table
- Fields include: commentId, requestId, commentText, commentedBy, commentedAt, isEtaChange, oldEta, newEta, changeReason, active

### 2. DTOs
- **CommentDTO.java**: For returning comment data
- **CreateCommentDTO.java**: For creating new comments
- **UpdateEtaDTO.java**: For updating ETA with reason

### 3. Repository
- **RequestCommentRepository.java**: JPA repository with methods:
  - `findByRequestIdAndActiveTrueOrderByCommentedAtAsc()`: Get all active comments for a request

### 4. Services

#### RequestCommentService
- `getCommentsByRequestId()`: Get all comments for a request
- `addComment()`: Add a regular comment
- `addEtaChangeComment()`: Add a comment when ETA is changed

**Permissions for Comments:**
- ADMIN: Can comment on any ticket
- SCRUM_MASTER: Can comment on any ticket
- Assigned User: Can comment on tickets assigned to them
- Creator: Can comment on tickets they created

#### RequestService (Updated)
- `updateEta()`: New method to update ETA with automatic comment creation
  - Validates permissions (ADMIN, SCRUM_MASTER, or assigned user)
  - Updates ETA
  - Automatically creates a comment with old ETA, new ETA, and reason

### 5. Controller Endpoints

#### RequestController (New Endpoints)

1. **PUT /api/requests/{id}/eta**
   - Update ETA for a ticket
   - Requires: UpdateEtaDTO (newEta, changeReason, commentText)
   - Headers: X-User-NTID
   - Permissions: ADMIN, SCRUM_MASTER, or assigned user

2. **GET /api/requests/{id}/comments**
   - Get all comments for a ticket
   - Headers: X-User-NTID
   - Returns: List of CommentDTO

3. **POST /api/requests/{id}/comments**
   - Add a comment to a ticket
   - Requires: CreateCommentDTO (commentText, isEtaChange, changeReason)
   - Headers: X-User-NTID
   - Permissions: ADMIN, SCRUM_MASTER, assigned user, or creator

## Frontend Implementation Required

### 1. Request Detail Component Updates

#### ETA Update Section
- Add "Edit ETA" button (visible to ADMIN, SCRUM_MASTER, or assigned user)
- When clicked, show:
  - Date/Time picker for new ETA
  - Text area for "Reason for change" (required)
  - Optional: Additional comment text
  - "Update ETA" button
- On update:
  - Call `PUT /api/requests/{id}/eta`
  - Show success message
  - Refresh comments section
  - Update ETA display

#### Comments Section
- Display all comments in chronological order (oldest first)
- Show for each comment:
  - Comment text
  - Commented by (user NTID)
  - Commented at (timestamp)
  - If ETA change: Show old ETA → new ETA and reason
- Add "Add Comment" button (visible to ADMIN, SCRUM_MASTER, assigned user, or creator)
- Comment form:
  - Text area for comment
  - "Add Comment" button
- On add:
  - Call `POST /api/requests/{id}/comments`
  - Refresh comments list

### 2. Service Updates

#### RequestService (Frontend)
Add methods:
```typescript
updateEta(requestId: number, updateEtaDTO: UpdateEtaDTO): Observable<any>
getComments(requestId: number): Observable<CommentDTO[]>
addComment(requestId: number, createCommentDTO: CreateCommentDTO): Observable<CommentDTO>
```

#### Interfaces
```typescript
interface CommentDTO {
  commentId: number;
  requestId: number;
  commentText: string;
  commentedBy: string;
  commentedAt: string;
  isEtaChange: boolean;
  oldEta?: string;
  newEta?: string;
  changeReason?: string;
}

interface UpdateEtaDTO {
  newEta: string; // ISO datetime string
  changeReason: string;
  commentText?: string;
}

interface CreateCommentDTO {
  commentText: string;
  isEtaChange?: boolean;
  changeReason?: string;
}
```

## Features

### ETA Update with Comments
1. **Who can update ETA:**
   - ADMIN
   - SCRUM_MASTER
   - Assigned user (the person the ticket is assigned to)

2. **What happens when ETA is updated:**
   - ETA field is updated in FLOWAI_REQUESTS table
   - A comment is automatically created in FLOWAI_REQUEST_COMMENTS with:
     - `isEtaChange = true`
     - Old ETA value
     - New ETA value
     - Change reason
     - Comment text: "ETA changed from [old] to [new]. Reason: [reason]"

### Comments System
1. **Who can comment:**
   - ADMIN: Any ticket
   - SCRUM_MASTER: Any ticket
   - Assigned user: Tickets assigned to them
   - Creator: Tickets they created

2. **Comment types:**
   - Regular comments: General discussion/notes
   - ETA change comments: Automatically created when ETA is updated

3. **Comment display:**
   - All comments shown in chronological order
   - ETA change comments show special formatting with old/new ETA values

## Database Migration

Run the SQL from `TABLE_STRUCTURE.sql` to create the new `FLOWAI_REQUEST_COMMENTS` table:

```sql
-- See TABLE_STRUCTURE.sql for complete CREATE statement
```

## Testing Checklist

- [ ] ADMIN can update ETA with reason
- [ ] SCRUM_MASTER can update ETA with reason
- [ ] Assigned user can update ETA with reason
- [ ] Other users cannot update ETA
- [ ] ETA update creates automatic comment
- [ ] Comments are visible in ticket detail view
- [ ] ADMIN can add comments
- [ ] SCRUM_MASTER can add comments
- [ ] Assigned user can add comments
- [ ] Creator can add comments
- [ ] Other users cannot add comments
- [ ] Comments display in chronological order
- [ ] ETA change comments show old/new ETA values

## Next Steps

1. **Frontend Implementation:**
   - Update `request-detail.component.ts` to add comment methods
   - Update `request-detail.component.html` to show comments section
   - Update `request-detail.component.scss` for styling
   - Add ETA update form with reason field
   - Add comments display section
   - Add comment form

2. **Service Updates:**
   - Add methods to `request.service.ts` for comments and ETA update

3. **Testing:**
   - Test all permission scenarios
   - Test ETA update with comments
   - Test comment creation
   - Test comment display
