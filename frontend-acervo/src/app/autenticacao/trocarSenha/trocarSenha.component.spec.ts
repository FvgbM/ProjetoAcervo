import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrocarSenhaComponent } from './trocarSenha.component';

describe('TrocarSenhaComponent', () => {
  let component: TrocarSenhaComponent;
  let fixture: ComponentFixture<TrocarSenhaComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TrocarSenhaComponent]
    });
    fixture = TestBed.createComponent(TrocarSenhaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
