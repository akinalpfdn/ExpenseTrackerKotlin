# Financial Planning Feature Development Documentation

## Project Objective
Implement a comprehensive financial planning module for the existing expense tracker application. The module must calculate net income projections, manage multiple plans, and provide detailed monthly breakdowns with inflation adjustment capabilities.

## Critical Requirements for Claude Code

### Mandatory Pre-Development Tasks
Claude Code must perform complete project analysis before any implementation:
- Analyze entire codebase structure and patterns
- Identify existing UI components and their implementations  
- Understand current state management approach
- Review database schema and data models
- Examine theme system and styling conventions
- Document existing naming conventions and architectural decisions

### Non-Negotiable Code Standards
- Maximum file length: 1000 lines per file
- Modular architecture is mandatory
- Must use existing color modules and object preferences
- Must follow established project patterns and conventions
- No deviation from existing architectural decisions without explicit approval

### Required Project Integration
- Fixed expenses must be retrieved from existing system data
- Monthly expense averages must be calculated from last 3 months of actual data
- All new components must integrate seamlessly with existing UI patterns
- Database operations must follow established patterns

## Feature Specifications

### Core Functionality Requirements
1. Fixed expenses automatic retrieval from system
2. Optional monthly spending averages from last 3 months
3. Optional inflation/interest rate application with user input
4. Multiple plan creation and management
5. Plan card overview interface
6. Modal bottom sheet for detailed plan view
7. Monthly breakdown showing income, expenses, and net results
8. Plan duration with start date and end date display
9. Current profit/loss calculation and display

### Technical Implementation Requirements

#### Data Structure Requirements
Plans must include:
- Unique identifier and name
- Start date and duration in months
- Inflation settings and rate if applicable
- Fixed income and expense integration
- Monthly calculation breakdowns
- Creation and modification timestamps

#### UI Component Requirements
Plan cards must display:
- Plan name and current status
- Start date and duration
- Current net profit/loss
- Quick action buttons

Detail modal must include:
- Plan metadata and settings
- Monthly breakdown table or list
- Current financial position
- Edit and management functions

#### Calculation Engine Requirements
- Real-time financial calculations
- Inflation adjustment capabilities
- Integration with historical expense data
- Monthly projection algorithms
- Cumulative result tracking

### File Structure Constraints
Maximum recommended file organization:
- Components: Individual files under 400 lines
- Hooks: Individual files under 300 lines
- Utils: Individual files under 500 lines
- Types: Individual files under 200 lines
- Main screen: Under 600 lines

### Development Approach Requirements

#### Phase 1: Analysis and Planning
Complete project codebase analysis including:
- Existing component patterns identification
- Current data flow understanding
- Theme and styling system documentation
- State management pattern analysis

#### Phase 2: Foundation Development
Create core infrastructure:
- Type definitions matching existing patterns
- Utility functions for calculations
- Custom hooks for data management
- Base component structure

#### Phase 3: UI Implementation  
Develop user interface components:
- Plan overview cards
- Plan creation interface
- Detail modal implementation
- Monthly breakdown display

#### Phase 4: Business Logic Integration
Implement core functionality:
- Fixed expense data integration
- Average calculation algorithms
- Inflation adjustment calculations
- Plan persistence and retrieval

#### Phase 5: Testing and Optimization
Final implementation phase:
- Calculation accuracy verification
- UI component testing
- Performance optimization
- Code review and refactoring

## Implementation Guidelines

### Code Quality Standards
All code must adhere to:
- Existing project naming conventions
- Established error handling patterns
- Current logging and debugging approaches
- Existing accessibility implementations
- Performance optimization standards

### Integration Requirements
New features must:
- Use existing database connection patterns
- Follow established API calling conventions
- Integrate with current navigation structure
- Maintain existing user experience patterns
- Preserve all current functionality

### Feedback Loop Process
Development must proceed through structured feedback:
1. Present analysis findings before implementation
2. Show incremental progress for approval
3. Demonstrate adherence to established patterns
4. Request feedback before proceeding to next phase
5. Document any deviations from original specifications

## Success Criteria
Implementation is considered successful when:
- All features function according to specifications
- Code adheres to established project standards
- No files exceed 1000 line limit
- Integration maintains existing functionality
- User experience remains consistent with current application
- Performance meets or exceeds current application standards

## Development Constraints
Claude Code must operate within these constraints:
- No modification of existing core functionality without approval
- No introduction of new dependencies without justification
- No architectural changes without explicit permission
- No styling system modifications without approval
- All changes must be backward compatible