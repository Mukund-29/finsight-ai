import { ErrorHandler, Injectable } from '@angular/core';

/**
 * Global Error Handler
 * Filters out browser extension errors from console
 * 
 * @author Mukund Kute
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: any): void {
    // Filter out browser extension errors
    const errorMessage = error?.message || '';
    const errorStack = error?.stack || '';
    
    // Check if error is from browser extension
    const isExtensionError = 
      errorMessage.includes('excalidraw') ||
      errorMessage.includes('chrome-extension://') ||
      errorMessage.includes('Content Security Policy') ||
      errorMessage.includes('ChunkLoadError') ||
      errorStack.includes('chrome-extension://') ||
      errorStack.includes('content.js');
    
    // Only log non-extension errors
    if (!isExtensionError) {
      console.error('Application Error:', error);
    }
    // Silently ignore extension errors
  }
}
