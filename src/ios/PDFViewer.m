//
//  PDFViewer.m
//  iOSPDFPlugin
//
//  Created by Manuel Mouta on 26/12/2016.
//
//

#import "PDFViewer.h"
#import <Cordova/CDV.h>
#import "PDFViewerViewController.h"

@implementation PDFViewer

- (void)openPdf:(CDVInvokedUrlCommand*)command
{
        id title = [command argumentAtIndex:0];
        id base64 = [command argumentAtIndex:1];
        NSArray *alertArray = [command argumentAtIndex:2];
    
    
        PDFViewerViewController* vc = [PDFViewerViewController initWithFileString:base64
                                                                     andTitle:title
                                                                withPlugin:self
                                                                withButtons: alertArray
                                                            andCallbackId:command.callbackId];
    
        [self.viewController presentViewController:vc animated:YES completion:nil];
}
@end
