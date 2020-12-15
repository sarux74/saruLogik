import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewRelationDialogComponent } from './new-relation-dialog.component';

describe('NewRelationDialogComponent', () => {
  let component: NewRelationDialogComponent;
  let fixture: ComponentFixture<NewRelationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewRelationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewRelationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
