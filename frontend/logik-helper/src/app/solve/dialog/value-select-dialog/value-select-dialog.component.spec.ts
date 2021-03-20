import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ValueSelectDialogComponent} from './value-select-dialog.component';

describe('ValueSelectDialogComponent', () => {
    let component: ValueSelectDialogComponent;
    let fixture: ComponentFixture<ValueSelectDialogComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ValueSelectDialogComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ValueSelectDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
