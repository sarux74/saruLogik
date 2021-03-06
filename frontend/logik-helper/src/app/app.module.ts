import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {GroupComponent} from './group/group.component';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {HttpClientModule} from '@angular/common/http';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatDialogModule} from '@angular/material/dialog';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatIconModule} from '@angular/material/icon';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SolveComponent} from './solve/solve.component';
import {DetektorComponent} from './detektor/detektor.component';
import {ValueSelectDialogComponent} from './solve/dialog/value-select-dialog/value-select-dialog.component';
import {NewBlockDialogComponent} from './edit/dialog/new-block-dialog/new-block-dialog.component';
import {NewRelationDialogComponent} from './edit/dialog/new-relation-dialog/new-relation-dialog.component';
import {MatSelectModule} from '@angular/material/select';
import {CompactViewComponent} from './compact-view/compact-view.component';
import {GroupViewComponent} from './group-view/group-view.component';
import {ChainViewComponent} from './chain-view/chain-view.component';
import {FileSaverModule} from 'ngx-filesaver';
import {ShowChangesDialogComponent} from './solve/dialog/show-changes/show-changes.component';
import {GroupEditDialogComponent} from './group/dialog/group-edit-dialog.component';
import {BlockCompareViewComponent} from './block-compare-view/block-compare-view.component';
import {ErrorDialogComponent} from './dialog/error-dialog/error-dialog.component';
import {ScrollingModule} from '@angular/cdk/scrolling';
import {MultipleRelationViewComponent} from './multiple-relation-view/multiple-relation-view.component';
import {CombinationViewComponent} from './combination-view/combination-view.component';
import {PositionerComponent} from './positioner/positioner.component';
import {EditComponent} from './edit/edit.component';

@NgModule({
    declarations: [
        AppComponent,
        GroupComponent,
        GroupEditDialogComponent,
        SolveComponent,
        DetektorComponent,
        ValueSelectDialogComponent,
        NewBlockDialogComponent,
        NewRelationDialogComponent,
        CompactViewComponent,
        GroupViewComponent,
        ChainViewComponent,
        ShowChangesDialogComponent,
        BlockCompareViewComponent,
        ErrorDialogComponent,
        MultipleRelationViewComponent,
        CombinationViewComponent,
        PositionerComponent,
        EditComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MatCardModule,
        MatListModule,
        HttpClientModule,
        FlexLayoutModule,
        MatDialogModule,
        FormsModule,
        ReactiveFormsModule,
        MatCheckboxModule,
        MatIconModule,
        MatFormFieldModule,
        MatSelectModule,
        FileSaverModule,
        MatInputModule,
        ScrollingModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {}
