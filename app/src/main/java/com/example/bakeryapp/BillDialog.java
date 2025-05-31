package com.example.bakeryapp;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BillDialog {

    public static void showBillDialog(Context context, String billId, boolean canGeneratePdf) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bill);

        TextView billHeader = dialog.findViewById(R.id.billHeader);
        TextView billDetails = dialog.findViewById(R.id.billDetails);
        TextView billItems = dialog.findViewById(R.id.billItems);
        TextView billTotal = dialog.findViewById(R.id.billTotal);
        Button printButton = dialog.findViewById(R.id.printButton);
        Button closeButton = dialog.findViewById(R.id.closeButton);

        // Disable print button if PDF generation is not allowed
        printButton.setEnabled(canGeneratePdf);
        if (!canGeneratePdf) {
            printButton.setText("Print (Permission Denied)");
        }

        DatabaseReference billRef = FirebaseDatabase.getInstance().getReference("bills").child(billId);
        billRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Bill bill = snapshot.getValue(Bill.class);
                if (bill == null) {
                    Toast.makeText(context, "Bill not found", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                billHeader.setText("BAKERY MANAGEMENT SYSTEM INVOICE");
                String details = String.format(Locale.getDefault(),
                        "Bill Number: %s\nOrder ID: %s\nDate: %s\nCustomer: %s\nAddress: %s\nPhone: %s\nPayment: %s",
                        bill.getBillNumber() != null ? bill.getBillNumber() : billId.substring(billId.length() - 6),
                        bill.getOrderId().substring(bill.getOrderId().length() - 6),
                        bill.getCompletionDate(),
                        bill.getCustomerEmail() != null ? bill.getCustomerEmail() : "N/A",
                        bill.getDeliveryAddress() != null ? bill.getDeliveryAddress() : "N/A",
                        bill.getPhoneNumber() != null ? bill.getPhoneNumber() : "N/A",
                        bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A");
                billDetails.setText(details);

                StringBuilder itemsStr = new StringBuilder("Items:\n");
                for (Item item : bill.getItems()) {
                    itemsStr.append(String.format(Locale.getDefault(), "%s x%d @ ₹%.2f = ₹%.2f\n",
                            item.getProduct(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
                }
                billItems.setText(itemsStr.toString());
                billTotal.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", bill.getTotalAmount()));

                if (canGeneratePdf) {
                    printButton.setOnClickListener(v -> {
                        try {
                            String fileName = "Bill_" + (bill.getBillNumber() != null ? bill.getBillNumber() : billId) + ".pdf";
                            OutputStream outputStream;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Use MediaStore for Android 10+
                                ContentResolver resolver = context.getContentResolver();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                                if (uri == null) {
                                    throw new Exception("Failed to create file");
                                }
                                outputStream = resolver.openOutputStream(uri);
                            } else {
                                // Use legacy storage for Android 9 and below
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                                outputStream = new FileOutputStream(file);
                            }

                            PdfWriter writer = new PdfWriter(outputStream);
                            PdfDocument pdf = new PdfDocument(writer);
                            Document document = new Document(pdf);

                            document.add(new Paragraph("BAKERY MANAGEMENT SYSTEM INVOICE"));
                            document.add(new Paragraph(details));
                            document.add(new Paragraph("\nItems:"));
                            float[] columnWidths = {200, 50, 100, 100};
                            Table table = new Table(columnWidths);
                            table.addCell("Product");
                            table.addCell("Qty");
                            table.addCell("Unit Price");
                            table.addCell("Total");
                            for (Item item : bill.getItems()) {
                                table.addCell(item.getProduct());
                                table.addCell(String.valueOf(item.getQuantity()));
                                table.addCell(String.format(Locale.getDefault(), "₹%.2f", item.getUnitPrice()));
                                table.addCell(String.format(Locale.getDefault(), "₹%.2f", item.getTotal()));
                            }
                            document.add(table);
                            document.add(new Paragraph(String.format(Locale.getDefault(), "Total: ₹%.2f", bill.getTotalAmount())));
                            document.close();
                            outputStream.close();
                            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                closeButton.setOnClickListener(v -> dialog.dismiss());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(context, "Error loading bill: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}