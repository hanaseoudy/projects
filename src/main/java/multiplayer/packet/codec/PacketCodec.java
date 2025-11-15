package multiplayer.packet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import multiplayer.packet.Packet;

import java.io.*;
import java.util.List;

public final class PacketCodec extends ByteToMessageCodec<Packet> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(packet);
            oos.flush();
        }
        
        byte[] data = baos.toByteArray();
        out.writeInt(data.length); // Write length prefix
        out.writeBytes(data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return; // Not enough bytes to read length
        }
        
        in.markReaderIndex();
        int length = in.readInt();
        
        if (in.readableBytes() < length) {
            in.resetReaderIndex(); // Reset to before reading length
            return; // Not enough bytes for full packet
        }
        
        byte[] data = new byte[length];
        in.readBytes(data);
        
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object obj = ois.readObject();
            if (obj instanceof Packet) {
                out.add(obj);
            } else {
                System.err.println("Unexpected object type: " + obj.getClass().getName());
            }
        }
    }
}