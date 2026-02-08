import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CreateRequestComponent } from './components/create-request/create-request.component';
import { RequestDetailComponent } from './components/request-detail/request-detail.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { ViewAllTicketsComponent } from './components/view-all-tickets/view-all-tickets.component';
import { UserStatisticsComponent } from './components/user-statistics/user-statistics.component';
import { CanDeactivateGuard } from './guards/can-deactivate.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent },
  { 
    path: 'dashboard/create', 
    component: CreateRequestComponent,
    canDeactivate: [CanDeactivateGuard]
  },
  { path: 'dashboard/request/:id', component: RequestDetailComponent },
  { path: 'dashboard/users', component: UserManagementComponent },
  { path: 'dashboard/statistics', component: UserStatisticsComponent },
  { path: 'dashboard/tickets/:type', component: ViewAllTicketsComponent },
  { path: '**', redirectTo: '/dashboard' }
];
