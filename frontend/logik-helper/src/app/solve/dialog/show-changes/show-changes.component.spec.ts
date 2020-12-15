import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowChangesComponent } from './show-changes.component';

describe('ShowChangesComponent', () => {
  let component: ShowChangesComponent;
  let fixture: ComponentFixture<ShowChangesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShowChangesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowChangesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
