import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AgentUiComponent } from './agent-ui.component';

describe('AgentUiComponent', () => {
  let component: AgentUiComponent;
  let fixture: ComponentFixture<AgentUiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgentUiComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AgentUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
