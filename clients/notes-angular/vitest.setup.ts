import 'zone.js';
import 'zone.js/testing';

import { getTestBed } from '@angular/core/testing';

try {
  getTestBed().initTestEnvironment([], null as any);
} catch (e) {
  // Ignore error if already initialized
}
