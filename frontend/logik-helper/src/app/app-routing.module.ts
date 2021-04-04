import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {GroupComponent} from './group/group.component';
import {SolveComponent} from './solve/solve.component';
import {DetektorComponent} from './detektor/detektor.component';
import {CompactViewComponent} from './compact-view/compact-view.component';
import {GroupViewComponent} from './group-view/group-view.component';
import {BlockCompareViewComponent} from './block-compare-view/block-compare-view.component';
import {MultipleRelationViewComponent} from './multiple-relation-view/multiple-relation-view.component';
import {CombinationViewComponent} from './combination-view/combination-view.component';
import {PositionerComponent} from './positioner/positioner.component';
import {EditComponent} from './edit/edit.component';

const routes: Routes = [
    {path: 'groups', component: GroupComponent},
    {path: '', redirectTo: '/groups', pathMatch: 'full'}, // redirect to `first-component`
    {path: 'edit', component: EditComponent},
    {path: 'solve', component: SolveComponent},
    {path: 'detektor', component: DetektorComponent},
    {path: 'compact', component: CompactViewComponent},
    {path: 'group', component: GroupViewComponent},
    {path: 'block', component: BlockCompareViewComponent},
    {path: 'multiple', component: MultipleRelationViewComponent},
    {path: 'combination', component: CombinationViewComponent},
    {path: 'positioner', component: PositionerComponent},
    {path: '**', component: GroupComponent},
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
    exports: [RouterModule]
})
export class AppRoutingModule {}