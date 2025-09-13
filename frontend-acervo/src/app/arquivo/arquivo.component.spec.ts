import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArquivoComponent } from './arquivo.component';

describe('ArquivoComponent', () => {
  let component: ArquivoComponent;
  let fixture: ComponentFixture<ArquivoComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
    declarations: [ArquivoComponent]
});
    fixture = TestBed.createComponent(ArquivoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
