//
//  BPIWebViewController.m
//  BPI App Shell
//
//  Created BPI
//  Copyright © 2016 BPI. All rights reserved.
//

#import "PdfViewerViewController.h"
#import "UIColor+BPIColor.h"

#define CUSTOM_ORANGE_COLOR [UIColor colorWithRed:255.0/255.0 green:102/255.0 blue:0/255.0 alpha:1]
#define CUSTOM_BLUE_COLOR [UIColor colorWithRed:0/255.0 green:0/255.0 blue:83/255.0 alpha:1]

@interface PDFViewerViewController ()

@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *loadingIndicator;
@property (weak, nonatomic) IBOutlet UIView *header;

@property (weak, nonatomic) IBOutlet UIButton *backBtn;
@property (weak, nonatomic) IBOutlet UILabel *titleLbl;
@property (weak, nonatomic) IBOutlet UIButton *shareBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterShare;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterBack;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterTitle;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintHeightHeader;

@property(strong, nonatomic) NSString *fileString;
@property(strong, nonatomic) NSString *viewTitle;

@property(strong, nonatomic) CDVPlugin *plugin;
@property(strong, nonatomic) NSString *callbackId;
@property(strong, nonatomic) NSArray *buttons;

@property(nonatomic) BOOL isWebViewLoaded;

// Offline Support
@property (strong, nonatomic) NSString *failedURL;

@property (strong, nonatomic) NSURLConnection* urlConnection;
@property (strong, nonatomic) NSURLRequest* urlRequest;

@property (weak, nonatomic) IBOutlet UIView *footerView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *footerHeight;

@property (weak, nonatomic) IBOutlet UIButton *btn1;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn1Width;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn1WidthEqualsBtn2Constraint;
@property (weak, nonatomic) IBOutlet UIButton *btn2;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn2Width;
@property (weak, nonatomic) IBOutlet UIButton *btn3;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn3Width;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn3WidthEqualsBtn1Constraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn3Leading;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn2Leading;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn1Leading;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *trailing;

@property (strong, nonatomic) NSArray* arrayButtons;

@end

@implementation PDFViewerViewController

#pragma mark - Convenience methods

+ (instancetype) initWithFileString:(NSString *)fileString
                    andTitle:(NSString *)aTitle
                  withPlugin:(CDVPlugin *)aPlugin
                        withButtons:(NSArray *) aButtons
               andCallbackId:(NSString *)aCallbackId
{
    PDFViewerViewController *vc = [[PDFViewerViewController alloc] init];
    if(vc)
    {
        vc.fileString = fileString;
        vc.viewTitle = aTitle;
        vc.plugin = aPlugin;
        vc.buttons = aButtons;
        vc.callbackId = aCallbackId;
    }

    return vc;
}

#pragma mark - UIViewController methods
- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.webView.delegate = self;

    // loads everything first into memory before rendering
    self.webView.suppressesIncrementalRendering = YES;

    self.webView.scalesPageToFit = YES;
    NSData* fileData = [[NSData alloc] initWithBase64EncodedString:self.fileString options:NSDataBase64DecodingIgnoreUnknownCharacters];
    [self.webView loadData:fileData MIMEType:@"application/pdf" textEncodingName:@"utf-8" baseURL:nil];


    //[self.view setNeedsUpdateConstraints];

}

- (void)viewDidAppear:(BOOL)animated
{
    [self setupTitleHeader];
    [super viewDidAppear:animated];
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.webView.delegate = self;
    UIColor *btnBorderColor = CUSTOM_BLUE_COLOR;
    [[self.btn1 layer] setBorderWidth:1.0];
    [[self.btn1 layer] setBorderColor:[[UIColor colorWithHexString:@"#000053"] CGColor]];
    [[self.btn2 layer] setBorderWidth:1.0];
    [[self.btn2 layer] setBorderColor:[[UIColor colorWithHexString:@"#000053"] CGColor]];
    [[self.btn3 layer] setBorderWidth:1.0];
    [[self.btn3 layer] setBorderColor:[[UIColor colorWithHexString:@"#000053"] CGColor]];
    self.arrayButtons = [NSArray arrayWithObjects: self.btn1,self.btn2,self.btn3, nil];
    [self setMyButtons:self.buttons];

    self.constraintHeightHeader.constant = self.constraintHeightHeader.constant + [UIApplication sharedApplication].statusBarFrame.size.height;
    self.constraintCenterBack.constant = [UIApplication sharedApplication].statusBarFrame.size.height / 2;
    [self.view setNeedsUpdateConstraints];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleDefault];
    UIView *statusBar = [[[UIApplication sharedApplication] valueForKey:@"statusBarWindow"] valueForKey:@"statusBar"];

    if ([statusBar respondsToSelector:@selector(setBackgroundColor:)]) {
        statusBar.backgroundColor = [UIColor clearColor];
    }
}


#pragma mark - IBAction methods

- (IBAction)back
{

    CATransition *transition = [CATransition animation];
    transition.duration = 0.3;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type = kCATransitionPush;
    transition.subtype = kCATransitionFromLeft;
    [self.view.window.layer addAnimation:transition forKey:nil];


    [self dismissViewControllerAnimated:YES
                             completion:^{
    [self removeFromParentViewController];

        if(!self.plugin) return;

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsDictionary:@{@"message": @"Modal closed natively, please close modal from client side aswell",@"type": @"CLOSE"}];

        [pluginResult setKeepCallbackAsBool:NO];
        [self.plugin.commandDelegate sendPluginResult:pluginResult
                                           callbackId:self.callbackId];
    }];
}

- (IBAction)share
{
    if (!self.isWebViewLoaded) return;

    NSURL *resourceUrl = self.webView.request.URL;

    NSString *pdfName = self.viewTitle;

    NSString *guidPDF = [NSString stringWithFormat:@"%@.pdf", pdfName];
    NSURL *url = [NSURL fileURLWithPath:[NSTemporaryDirectory() stringByAppendingString:guidPDF]];
    NSData *pdfData = [NSData dataWithContentsOfURL:resourceUrl];

    [pdfData writeToURL:url atomically:NO];

    UIActivityViewController *activityViewController = [[UIActivityViewController alloc] initWithActivityItems:@[url]
                                                                                         applicationActivities:nil];

    [activityViewController setCompletionWithItemsHandler:^(NSString *activityType, BOOL completed, NSArray *returnedItems, NSError *activityError) {
        NSError *errorBlock;
        if([[NSFileManager defaultManager] removeItemAtURL:url error:&errorBlock] == NO) {
            NSLog(@"error deleting file %@", errorBlock.localizedDescription);
            return;
        }
    }];

    [self presentViewController:activityViewController
                       animated:YES
                     completion:nil];

}

- (IBAction)buttonAction : (UIButton*) sender
{
    CATransition *transition = [CATransition animation];
    transition.duration = 0.3;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type = kCATransitionPush;
    transition.subtype = kCATransitionFromLeft;
    [self.view.window.layer addAnimation:transition forKey:nil];


    [self dismissViewControllerAnimated:YES
                             completion:^{
                                 [self removeFromParentViewController];

                                 if(!self.plugin) return;

                                 CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                     messageAsDictionary:@{@"message": [NSString stringWithFormat:@"%ld", (long)sender.tag]}];
                                 
                                 [pluginResult setKeepCallbackAsBool:NO];
                                 [self.plugin.commandDelegate sendPluginResult:pluginResult
                                                                    callbackId:self.callbackId];
                             }];
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    [self.loadingIndicator startAnimating];
    self.isWebViewLoaded = NO;
}

-(void)webViewDidFinishLoad:(UIWebView *)webView
{
    if(webView.isLoading) return;
    
    [self.loadingIndicator stopAnimating];
    self.isWebViewLoaded = YES;
}

#pragma mark - Utilities
- (void)setupTitleHeader
{
    UIStatusBarStyle statusbarStyle =UIStatusBarStyleLightContent;
    UIColor *statusbarBackgroundColor =CUSTOM_ORANGE_COLOR;
    
    [[UIApplication sharedApplication] setStatusBarStyle:statusbarStyle animated:YES];
    
    UIView *statusBar = [[[UIApplication sharedApplication] valueForKey:@"statusBarWindow"] valueForKey:@"statusBar"];
    if ([statusBar respondsToSelector:@selector(setBackgroundColor:)])
        statusBar.backgroundColor = statusbarBackgroundColor;

    [self setNeedsStatusBarAppearanceUpdate];
    [self.titleLbl setText:self.viewTitle];
    [self.titleLbl setFont:[UIFont fontWithName:@"TradeGothicBPI Bold" size:17]];
}

-(void) showButtonsFromListSize: (int) size
{
    switch (size) {
        case 0:
            //hide all buttons
            self.footerHeight.constant = 0;
            self.btn1.hidden = YES;
            self.btn2.hidden = YES;
            self.btn3.hidden = YES;
            break;
        case 1:
            //hide button 3
            self.btn3Width.constant = 0;
            self.btn3.hidden = YES;
            self.btn3WidthEqualsBtn1Constraint.active = FALSE;
            self.btn3Leading.constant = 0;
            self.btn1Leading.priority = 1000;
            
            //hide button 2
            self.btn2Width.constant = 0;
            self.btn2.hidden = YES;
            self.btn1WidthEqualsBtn2Constraint.active = FALSE;
            self.btn2Leading.constant = 0;
            self.btn2Leading.priority = 1000;

            break;
        case 2:
            self.btn3Width.constant = 0;
            self.btn3.hidden = YES;
            self.btn3WidthEqualsBtn1Constraint.active = FALSE;
            self.btn3Leading.constant = 0;
            self.btn1Leading.priority = 1000;
            break;
        case 3:
            break;
    }
}

-(void) setMyButtons:(NSArray *)buttons
{
    UIColor *btnBackgroundColor = CUSTOM_BLUE_COLOR;
    
    [self showButtonsFromListSize:[buttons count]];
    int i = 0;
    for (NSDictionary *buttonSettings in buttons)
    {
        UIButton* currentBtn = (UIButton*)(self.arrayButtons[i]);
        
        NSString* name = (NSString*)(buttonSettings[@"name"]);
        NSString *isDefault = (buttonSettings[@"isDefault"]);
        if([isDefault isEqualToString:@"true"]){
            [currentBtn setTitleColor:[UIColor whiteColor ] forState:UIControlStateNormal];
            currentBtn.backgroundColor = btnBackgroundColor;
        }
        int btnID = [buttonSettings[@"id"] integerValue];
        currentBtn.tag = btnID;
        [currentBtn setTitle:name forState:UIControlStateNormal];
        ++i;
    }
}

@end
