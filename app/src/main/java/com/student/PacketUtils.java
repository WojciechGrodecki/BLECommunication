package com.student;

import android.support.annotation.NonNull;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;

public class PacketUtils {
    private static final String PACKET_START = "<";
        public static final String PACKET_END = ">";
        private static final String PACKET_SEPARATOR = ":";

        public @NonNull
        String parse(@NonNull MessagePacket messagePacket) {
            return PACKET_START +
                    messagePacket.getPayload() +
                    PACKET_SEPARATOR +
                    messagePacket.getData() +
                    PACKET_SEPARATOR +
                    messagePacket.getCrc16() +
                    PACKET_END;
        }

        public @NonNull MessagePacket parse(@NonNull String message)
                throws UnsupportedEncodingException, InvalidObjectException {
            int packetStart = message.indexOf(PACKET_START);
            int firstSeparator = message.indexOf(PACKET_SEPARATOR);
            int secondSeparator = message.indexOf(PACKET_SEPARATOR, firstSeparator + 1);
            int packetEnd = message.indexOf(PACKET_END);

            int payload = Integer.parseInt(message.substring(packetStart + 1, firstSeparator));
            String data = message.substring(firstSeparator + 1, secondSeparator);
            String crc16 = message.substring(secondSeparator + 1, packetEnd);

            return new MessagePacket(payload, data.getBytes(), crc16);
        }
}
