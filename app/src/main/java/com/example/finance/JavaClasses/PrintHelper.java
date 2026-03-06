package com.example.finance.JavaClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrintHelper {
    private Context context;

    public PrintHelper(Context context) {
        this.context = context;
    }

    public void printSlip(String slipContent) {
        Log.d("PrintHelper", "Starting print process...");

        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) {
            Log.e("PrintHelper", "PrintManager is null!");
            return;
        }

        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 android.os.CancellationSignal cancellationSignal,
                                 LayoutResultCallback callback, android.os.Bundle extras) {
                Log.d("PrintHelper", "onLayout called");
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("receipt.pdf");
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(1);
                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                android.os.CancellationSignal cancellationSignal,
                                WriteResultCallback callback) {
                try {
                    Log.d("PrintHelper", "onWrite called");
                    PdfDocument pdfDocument = new PdfDocument();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    Paint paint = new Paint();
                    paint.setTextSize(20);
                    canvas.drawText(slipContent, 100, 100, paint);
                    pdfDocument.finishPage(page);
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                    pdfDocument.close();
                    Log.d("PrintHelper", "Printing successful!");
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (IOException e) {
                    Log.e("PrintHelper", "Printing failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        Log.d("PrintHelper", "Sending to printer...");
        printManager.print("POS Receipt", printAdapter, null);
    }
}
