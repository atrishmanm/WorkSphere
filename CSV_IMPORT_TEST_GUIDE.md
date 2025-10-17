# 📋 CSV Import Feature - Testing Guide

## 🎯 Quick Start Testing

### Application is RUNNING! ✅

The application is already launched. You should see the WorkSphere window with:
- Login screen OR
- Main application with tabs: Dashboard, Tasks, Kanban, Pomodoro, etc.

---

## 🔐 Step 1: Login (if needed)

If you see the login screen, use:
- **Username:** `admin`
- **Password:** `admin123`

Other test accounts:
- john_doe / john123
- jane_smith / jane123
- mike_jones / mike123

---

## 📊 Step 2: Go to Tasks Tab

1. Click the **"Tasks"** tab at the top
2. You'll see the task list with existing tasks
3. Look for the button toolbar at the top with buttons:
   - New Task
   - Edit
   - Delete
   - Refresh
   - Export
   - **Import CSV** ⬅️ **THIS IS THE NEW BUTTON (Orange color)**

---

## 🧪 Step 3: Test CSV Import - Full Workflow

### Option A: Generate Sample Template First (Recommended)

1. **Click the "Import CSV" button** (orange button)
   - A dialog window will open titled "Import Tasks from CSV"

2. **Click "📄 Generate Sample Template"**
   - A file save dialog will open
   - Save as: `sample_tasks.csv`
   - Location: Anywhere you like (Desktop is fine)
   - Click "Save"
   - You'll see: "Sample template generated successfully"

3. **Open the template file**
   - Open `sample_tasks.csv` in Excel, Notepad, or any text editor
   - You'll see 5 example tasks with proper format:
   ```csv
   title,description,priority,status,due_date,assigned_to_username,estimated_hours
   "Setup Development Environment","Install IDE, configure workspace, setup version control","HIGH","TODO","2025-10-25","john_doe","3"
   "Design Database Schema","Create ER diagram, define tables, relationships, and indexes","URGENT","IN_PROGRESS","2025-10-22","jane_smith","5"
   "Implement REST API","Build RESTful endpoints with proper authentication","MEDIUM","TODO","2025-10-30","john_doe","8"
   "Write Unit Tests","Create comprehensive test suite with 80% coverage","LOW","TODO","2025-11-05","mike_jones","6"
   "Deploy to Production","Setup CI/CD pipeline and deploy application","HIGH","TODO","2025-11-15","admin","4"
   ```

4. **Modify the template** (Optional - for testing)
   - Add your own tasks
   - Change priorities, dates, descriptions
   - **Important:** Keep the header row intact
   - **Important:** Use correct usernames: admin, john_doe, jane_smith, mike_jones
   - **Important:** Date format: yyyy-MM-dd (e.g., 2025-10-25)
   - **Important:** Priority: LOW, MEDIUM, HIGH, or URGENT
   - **Important:** Status: TODO, IN_PROGRESS, or COMPLETED

5. **Save the file** as CSV

6. **Go back to the Import dialog**
   - Click "Browse" button
   - Select your `sample_tasks.csv` file
   - Click "Open"
   - The file path will appear in the text field

7. **Click "⬆ Import Tasks"**
   - Progress bar will show
   - Wait for the import to complete (should be quick)
   - Results will show:
     ```
     Import completed!
     
     Successfully imported: 5 tasks
     Errors: 0
     
     Import completed successfully!
     ```

8. **Click "Close"**

9. **Check the task list**
   - Your tasks should now appear in the list!
   - They will be sorted by priority (URGENT first)

---

### Option B: Create Your Own CSV

1. **Create a new file** called `my_tasks.csv`

2. **Add this content:**
   ```csv
   title,description,priority,status,due_date,assigned_to_username,estimated_hours
   "My First CSV Task","Testing the CSV import feature","HIGH","TODO","2025-10-30","admin","2"
   "Another Test Task","This should also work","MEDIUM","TODO","2025-11-05","john_doe","3"
   "Third Task","One more for good measure","LOW","TODO","2025-11-10","jane_smith","1"
   ```

3. **Follow steps 6-9 from Option A**

---

## 🧪 Step 4: Test Error Handling

Let's test that validation works correctly!

1. **Create a file** called `bad_tasks.csv`

2. **Add INVALID data:**
   ```csv
   title,description,priority,status,due_date,assigned_to_username,estimated_hours
   "","This has no title","HIGH","TODO","2025-10-25","admin","3"
   "Bad Priority","This has invalid priority","SUPER_HIGH","TODO","2025-10-25","admin","3"
   "Bad Status","This has invalid status","HIGH","PENDING","2025-10-25","admin","3"
   "Bad Date","This has wrong date format","HIGH","TODO","10/25/2025","admin","3"
   "Bad User","This user doesn't exist","HIGH","TODO","2025-10-25","nonexistent_user","3"
   "Bad Hours","Hours is not a number","HIGH","TODO","2025-10-25","admin","not_a_number"
   ```

3. **Import this file**
   - Click "Import CSV"
   - Browse and select `bad_tasks.csv`
   - Click "⬆ Import Tasks"

4. **Check the error messages:**
   - You should see errors like:
     ```
     Import completed!
     
     Successfully imported: 0 tasks
     Errors: 6
     
     Line 2: Title cannot be empty
     Line 3: Invalid priority value: SUPER_HIGH. Must be one of: LOW, MEDIUM, HIGH, URGENT
     Line 4: Invalid status value: PENDING. Must be one of: TODO, IN_PROGRESS, COMPLETED
     Line 5: Invalid date format: 10/25/2025. Use yyyy-MM-dd
     Line 6: User not found: nonexistent_user
     Line 7: Invalid estimated hours: not_a_number
     ```

5. **This confirms validation is working! ✅**

---

## ✅ What Should Work

- ✅ Generate sample template saves a CSV file with 5 examples
- ✅ Browse button opens file chooser filtered to .csv files
- ✅ Import button is disabled until file is selected
- ✅ Progress bar shows during import
- ✅ Valid tasks are imported successfully
- ✅ Invalid tasks show specific error messages with line numbers
- ✅ Task list refreshes automatically after import
- ✅ Imported tasks appear sorted by priority

---

## 🎨 Visual Checklist

When testing, verify:

### Import Dialog Appearance:
- [ ] Dialog has title: "Import Tasks from CSV"
- [ ] Instructions are clear (4 steps listed)
- [ ] Three buttons at top: "📄 Generate Sample Template", "Browse", "⬆ Import Tasks"
- [ ] Text field shows selected file path
- [ ] Progress bar (empty initially)
- [ ] Results text area (empty initially)
- [ ] Close button at bottom

### Button States:
- [ ] Import button is DISABLED (gray) when no file selected
- [ ] Import button becomes ENABLED (blue) after selecting file
- [ ] Generate Template works independently

### During Import:
- [ ] Progress bar shows activity (indeterminate animation)
- [ ] Results area shows "Importing tasks..."
- [ ] UI stays responsive (doesn't freeze)

### After Import:
- [ ] Progress bar shows completion (100%)
- [ ] Results area shows:
  - Success count
  - Error count
  - Detailed error messages (if any)
- [ ] Task list in background refreshes automatically

---

## 📝 Sample Test Data

Here's some test data you can copy directly:

### Perfect Data (Should Import Successfully):
```csv
title,description,priority,status,due_date,assigned_to_username,estimated_hours
"Create Login Page","Design and implement user login with authentication","URGENT","IN_PROGRESS","2025-10-20","admin","4"
"Setup Database","Initialize PostgreSQL database with migrations","HIGH","COMPLETED","2025-10-18","john_doe","6"
"Write Documentation","Create user guide and API documentation","MEDIUM","TODO","2025-11-01","jane_smith","8"
"Code Review","Review pull requests from team members","LOW","TODO","2025-10-28","mike_jones","2"
```

### Mixed Data (Some Good, Some Bad):
```csv
title,description,priority,status,due_date,assigned_to_username,estimated_hours
"Good Task","This should work fine","HIGH","TODO","2025-10-25","admin","3"
"","This will fail - no title","MEDIUM","TODO","2025-10-26","admin","2"
"Another Good Task","This should also work","LOW","TODO","2025-10-27","john_doe","5"
"Bad Priority Task","This will fail - bad priority","CRITICAL","TODO","2025-10-28","admin","4"
```

**Expected Result:** 2 successful imports, 2 errors with explanations

---

## 🐛 Common Issues & Solutions

### Issue: "Import CSV button doesn't appear"
**Solution:** Make sure you're on the **Tasks** tab, not Dashboard or other tabs

### Issue: "User not found" errors
**Solution:** Use these exact usernames:
- admin
- john_doe
- jane_smith
- mike_jones

### Issue: "Invalid date format"
**Solution:** Use yyyy-MM-dd format:
- ✅ Correct: 2025-10-25
- ❌ Wrong: 10/25/2025
- ❌ Wrong: 25-10-2025

### Issue: File chooser shows all files, not just CSV
**Solution:** This is fine! The filter is set, but Windows allows showing all files. Just select .csv files.

### Issue: Excel changes date formats
**Solution:** 
1. Open CSV in Notepad instead, OR
2. In Excel, format date column as "Text" before entering dates, OR
3. Use the generated template which has correct formatting

---

## 🎉 Success Indicators

You'll know it's working when:

1. ✅ Orange "Import CSV" button appears in Tasks tab
2. ✅ Clicking opens a professional dialog
3. ✅ Template generates with 5 sample tasks
4. ✅ Valid CSV imports successfully
5. ✅ Invalid CSV shows specific error messages
6. ✅ Task list updates with new tasks
7. ✅ Tasks are visible and can be clicked/edited

---

## 📸 Screenshots to Take (for Documentation)

If you want to document the feature:

1. **Tasks tab with Import CSV button** (toolbar)
2. **Import dialog initial state** (empty)
3. **Sample template in Excel/Notepad**
4. **Import dialog with file selected**
5. **Progress bar during import**
6. **Success results** (after valid import)
7. **Error results** (after invalid import)
8. **Task list with imported tasks**

---

## 🚀 Next Steps After Testing

Once CSV import is working:

1. **Test with large files** (100+ tasks) to verify performance
2. **Test with special characters** in descriptions (quotes, commas)
3. **Export existing tasks** to CSV (future feature)
4. **Move on to implementing Enhanced Task Dialog** with subtasks!

---

**Happy Testing! 🎊**

If you encounter any issues, check the console output in VS Code's terminal for error details.
