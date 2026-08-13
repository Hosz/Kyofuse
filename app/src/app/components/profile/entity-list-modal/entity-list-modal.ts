import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ModalComponent } from '../../shared/modal/modal';
import { ProfileEntity } from '../../../shared/models/profile.model';

@Component({
  selector: 'app-entity-list-modal',
  imports: [ModalComponent, RouterLink],
  templateUrl: './entity-list-modal.html',
  styleUrl: './entity-list-modal.css',
})
export class EntityListModalComponent {
  open = input(false);
  title = input.required<string>();
  items = input<ProfileEntity[]>([]);

  closed = output<void>();
  itemClick = output<ProfileEntity>();
}
