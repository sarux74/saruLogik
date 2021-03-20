import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CombinationViewComponent} from './combination-view.component';

describe('CombinationViewComponent', () => {
    let component: CombinationViewComponent;
    let fixture: ComponentFixture<CombinationViewComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [CombinationViewComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(CombinationViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
