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

const routes: Routes = [
    {path: 'group', component: GroupComponent},
    {path: '', redirectTo: '/group', pathMatch: 'full'}, // redirect to `first-component`
    {path: 'solve', component: SolveComponent},
    {path: 'detektor', component: DetektorComponent},
    {path: 'view/compact', component: CompactViewComponent},
    {path: 'view/group', component: GroupViewComponent},
    {path: 'view/block', component: BlockCompareViewComponent},
    {path: 'view/multiple', component: MultipleRelationViewComponent},
    {path: 'view/combination', component: CombinationViewComponent},
    {path: 'view/positioner', component: PositionerComponent},
    {path: '**', component: GroupComponent},
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {}
