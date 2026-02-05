import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Filter out browser extension errors from console
const originalError = console.error;
console.error = function(...args: any[]) {
  const errorMessage = args.join(' ');
  // Filter out extension-related errors
  if (
    !errorMessage.includes('excalidraw') &&
    !errorMessage.includes('chrome-extension://') &&
    !errorMessage.includes('Content Security Policy') &&
    !errorMessage.includes('ChunkLoadError') &&
    !errorMessage.includes('content.js')
  ) {
    originalError.apply(console, args);
  }
};

// Filter out CSP warnings
const originalWarn = console.warn;
console.warn = function(...args: any[]) {
  const warningMessage = args.join(' ');
  // Filter out CSP warnings from extensions
  if (
    !warningMessage.includes('Content Security Policy') &&
    !warningMessage.includes('chrome-extension://') &&
    !warningMessage.includes('excalidraw')
  ) {
    originalWarn.apply(console, args);
  }
};

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
