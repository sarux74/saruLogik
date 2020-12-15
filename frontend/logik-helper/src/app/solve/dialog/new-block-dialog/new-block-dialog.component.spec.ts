import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewBlockDialogComponent } from './new-block-dialog.component';

describe('NewBlockDialogComponent', () => {
  let component: NewBlockDialogComponent;
  let fixture: ComponentFixture<NewBlockDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewBlockDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewBlockDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
