import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DashboardSection, UsuarioResponse } from '../../core/models';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  @Input({ required: true }) currentUser!: UsuarioResponse;
  @Input() isAdmin = false;
  @Input() activeSection: DashboardSection = 'overview';
  @Input() usersCount = 0;
  @Input() addressesCount = 0;

  @Output() readonly sectionChange = new EventEmitter<DashboardSection>();
  @Output() readonly profileClick = new EventEmitter<void>();
  @Output() readonly signOut = new EventEmitter<void>();
}
