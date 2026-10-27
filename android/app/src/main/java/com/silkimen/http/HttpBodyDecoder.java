package com.silkimen.http;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class HttpBodyDecoder {
    private static final String[] ACCEPTED_CHARSETS = {"UTF-8", "ISO-8859-1"};

    public static String decodeBody(byte[] bArr, String str) throws CharacterCodingException {
        return decodeBody(ByteBuffer.wrap(bArr), str);
    }

    public static String decodeBody(ByteBuffer byteBuffer, String str) throws CharacterCodingException {
        if (str == null) {
            return tryDecodeByteBuffer(byteBuffer);
        }
        return decodeByteBuffer(byteBuffer, str);
    }

    private static String tryDecodeByteBuffer(ByteBuffer byteBuffer) throws CharacterCodingException {
        int i = 0;
        while (true) {
            String[] strArr = ACCEPTED_CHARSETS;
            if (i < strArr.length - 1) {
                try {
                    return decodeByteBuffer(byteBuffer, strArr[i]);
                } catch (MalformedInputException | CharacterCodingException unused) {
                    i++;
                }
            } else {
                return decodeBody(byteBuffer, strArr[strArr.length - 1]);
            }
        }
    }

    private static String decodeByteBuffer(ByteBuffer byteBuffer, String str) throws CharacterCodingException {
        return createCharsetDecoder(str).decode(byteBuffer).toString();
    }

    private static CharsetDecoder createCharsetDecoder(String str) {
        return Charset.forName(str).newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    }
}