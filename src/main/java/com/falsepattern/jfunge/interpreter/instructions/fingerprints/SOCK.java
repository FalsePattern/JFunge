package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SOCK implements Fingerprint {
    public static final SOCK INSTANCE = new SOCK();
    private static final int AF_UNIX = 1;
    private static final int AF_INET = 2;
    private static final int SO_DEBUG = 1;
    private static final int SO_REUSEADDR = 2;
    private static final int SO_KEEPALIVE = 3;
    private static final int SO_DONTROUTE = 4;
    private static final int SO_BROADCAST = 5;
    private static final int OOBINLINE = 6;
    private static final int PF_UNIX = 1;
    private static final int PF_INET = 2;
    private static final int SOCK_DGRAM = 1;
    private static final int SOCK_STREAM = 2;
    private static final int PROTO_TCP = 1;
    private static final int PROTO_UDP = 2;
    private static final Map<Integer, SocketWrapper> sockets = Collections.synchronizedMap(new HashMap<>());
    private static final AtomicInteger indexCounter = new AtomicInteger(0);

    @Instr('A')
    public static void accept(ExecutionContext ctx) {
        val stack = ctx.stack();
        val s = stack.pop();

        val socket = sockets.get(s);
        if (socket == null) {
            ctx.IP().reflect();
            return;
        }
        try {
            val newSocket = socket.accept();
            val newIndex = indexCounter.getAndIncrement();
            sockets.put(newIndex, newSocket);
            val ip = newSocket.bindAddress.getAddress().getAddress();
            var ipInt = 0;
            for (int i = 0; i < 4; i++) {
                ipInt = ipInt * 256 + (ip[i] & 0xFF);
            }
            stack.push(newSocket.bindAddress.getPort());
            stack.push(ipInt);
            stack.push(newIndex);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('B')
    public static void bind(ExecutionContext ctx) {
        val stack = ctx.stack();
        val addr = stack.pop();
        val prt = stack.pop();
        val ct = stack.pop();
        val s = stack.pop();

        if (ct == AF_UNIX) {
            ctx.IP().reflect();
            return;
        }

        val socket = sockets.get(s);
        if (socket == null) {
            ctx.IP().reflect();
            return;
        }

        try {
            socket.bind(new InetSocketAddress(InetAddress.getByAddress(
                    new byte[]{(byte) (addr >> 24), (byte) (addr >> 16), (byte) (addr >> 8), (byte) addr}), prt));
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('C')
    public static void connect(ExecutionContext ctx) {
        val stack = ctx.stack();
        val addr = stack.pop();
        val prt = stack.pop();
        val ct = stack.pop();
        val s = stack.pop();

        if (ct == AF_UNIX) {
            ctx.IP().reflect();
            return;
        }

        val socket = sockets.get(s);
        if (socket == null) {
            ctx.IP().reflect();
            return;
        }

        try {
            socket.connect(new InetSocketAddress(InetAddress.getByAddress(
                    new byte[]{(byte) (addr >> 24), (byte) (addr >> 16), (byte) (addr >> 8), (byte) addr}), prt));
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('I')
    public static void parseIP(ExecutionContext ctx) {
        val stack = ctx.stack();
        val ipString = stack.popString();
        try {
            val ip = Arrays.stream(ipString.split("\\.")).mapToInt(Integer::parseInt).reduce(0, (a, b) -> a * 256 + b);
            stack.push(ip);
        } catch (NumberFormatException e) {
            ctx.IP().reflect();
        }
    }

    @Instr('K')
    public static void kill(ExecutionContext ctx) {
        val s = ctx.stack().pop();
        val socket = sockets.get(s);
        if (socket == null) {
            ctx.IP().reflect();
            return;
        }
        try {
            socket.kill();
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('L')
    public static void listen(ExecutionContext ctx) {
        val stack = ctx.stack();
        val s = stack.pop();
        val backlog = stack.pop();

        val socket = sockets.get(s);
        if (socket == null) {
            ctx.IP().reflect();
            return;
        }
        try {
            socket.listen(backlog);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('O')
    public static void setOption(ExecutionContext ctx) {
        val stack = ctx.stack();
        val s = stack.pop();
        val o = stack.pop();
        val n = stack.pop();

        val realOpt = switch (o) {
            default -> null;
            case SO_REUSEADDR -> StandardSocketOptions.SO_REUSEADDR;
            case SO_KEEPALIVE -> StandardSocketOptions.SO_KEEPALIVE;
            case SO_BROADCAST -> StandardSocketOptions.SO_BROADCAST;
        };
        if (realOpt == null) {
            ctx.IP().reflect();
        }
        try {
            val socket = sockets.get(s);
            if (socket != null) {
                socket.setOption(realOpt, n != 0);
            } else {
                ctx.IP().reflect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('R')
    public static void readSocket(ExecutionContext ctx) {
        val stack = ctx.stack();
        val s = stack.pop();
        val l = stack.pop();
        @Cleanup val mStack = MemoryStack.stackPush();
        val ptr = mStack.vec3i();
        stack.popVecDimProof(ctx.dimensions(), ptr);
        ptr.add(ctx.IP().storageOffset());

        val socket = sockets.get(s);
        try {
            val buf = new byte[l];
            val readLength = socket.receive(buf, l);
            val fs = ctx.fungeSpace();
            var x = ptr.x();
            val y = ptr.y();
            val z = ptr.z();
            for (int i = 0; i < readLength; i++) {
                fs.set(x + i, y, z, buf[i]);
            }
            stack.push(readLength);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Instr('S')
    public static void createSocket(ExecutionContext ctx) {
        val stack = ctx.stack();
        val pro = stack.pop();
        val typ = stack.pop();
        val pf = stack.pop();
        if (pf == PF_UNIX) {
            ctx.IP().reflect();
            return;
        }

        switch (typ) {
            case SOCK_STREAM -> {
                switch (pro) {
                    case PROTO_TCP -> {
                        val key = indexCounter.getAndIncrement();
                        sockets.put(key, new SocketWrapper(SOCK_STREAM));
                        stack.push(key);
                    }
                    case PROTO_UDP -> ctx.IP().reflect();
                }
            }
            case SOCK_DGRAM -> {
                switch (pro) {
                    case PROTO_TCP -> ctx.IP().reflect();
                    case PROTO_UDP -> {
                        val key = indexCounter.getAndIncrement();
                        sockets.put(key, new SocketWrapper(SOCK_DGRAM));
                        stack.push(key);
                    }
                }
            }
        }
    }

    @Instr('W')
    public static void writeSocket(ExecutionContext ctx) {
        val stack = ctx.stack();
        val s = stack.pop();
        val l = stack.pop();
        @Cleanup val mStack = MemoryStack.stackPush();
        val ptr = mStack.vec3i();
        stack.popVecDimProof(ctx.dimensions(), ptr);
        ptr.add(ctx.IP().storageOffset());

        val socket = sockets.get(s);
        if (socket == null) {
            ctx.IP().reflect();
            return;
        }
        val fs = ctx.fungeSpace();
        val data = new byte[l];
        var x = ptr.x();
        val y = ptr.y();
        val z = ptr.z();
        for (int i = 0; i < l; i++) {
            data[i] = (byte) fs.get(x + i, y, z);
        }
        try {
            stack.push(socket.send(data));
        } catch (IOException e) {
            e.printStackTrace();
            ctx.IP().reflect();
        }
    }

    @Override
    public int code() {
        return 0x534f434b;
    }

    private static class SocketWrapper {
        private final int kind;
        private final Map<SocketOption<Boolean>, Boolean> options = new HashMap<>();
        private ServerSocket streamListen = null;
        private Socket streamConn = null;
        private DatagramSocket dgram = null;
        private InetSocketAddress bindAddress = null;

        public SocketWrapper(int kind) {
            this.kind = kind;
        }

        private ServerSocket streamListen() throws IOException {
            if (streamConn != null || dgram != null) {
                throw new IOException("Invalid socket type");
            }
            if (streamListen == null) {
                streamListen = new ServerSocket();
                for (val entry : options.entrySet()) {
                    streamListen.setOption(entry.getKey(), entry.getValue());
                }
            }
            return streamListen;
        }

        private Socket streamConn() throws IOException {
            if (streamListen != null || dgram != null) {
                throw new IOException("Invalid socket type");
            }
            if (streamConn == null) {
                streamConn = new Socket();
                for (val entry : options.entrySet()) {
                    streamConn.setOption(entry.getKey(), entry.getValue());
                }
            }
            return streamConn;
        }

        private DatagramSocket dgram() throws IOException {
            if (streamListen != null || streamConn != null) {
                throw new IOException("Invalid socket type");
            }
            if (dgram == null) {
                dgram = new DatagramSocket();
                for (val entry : options.entrySet()) {
                    dgram.setOption(entry.getKey(), entry.getValue());
                }
                if (bindAddress != null) {
                    dgram.bind(bindAddress);
                }
            }
            return dgram;
        }

        public void setOption(SocketOption<Boolean> option, boolean value) throws IOException {
            if (streamListen != null) {
                streamListen.setOption(option, value);
            }
            if (streamConn != null) {
                streamConn.setOption(option, value);
            }
            if (dgram != null) {
                dgram.setOption(option, value);
            }
            options.put(option, value);
        }

        public SocketWrapper accept() throws IOException {
            if (kind == SOCK_STREAM) {
                if (streamListen == null) {
                    throw new IOException("Socket not listening");
                }
                val newSocket = streamListen.accept();
                val sock = new SocketWrapper(SOCK_STREAM);
                sock.bindAddress = bindAddress;
                sock.streamConn = newSocket;
                return sock;
            }
            throw new IllegalStateException("Invalid socket type");
        }

        public void bind(InetSocketAddress address) throws IOException {
            bindAddress = address;
            if (kind == SOCK_DGRAM) {
                dgram();
            }
        }

        public void connect(InetSocketAddress address) throws IOException {
            switch (kind) {
                case SOCK_STREAM -> streamConn().connect(address);
                case SOCK_DGRAM -> dgram().connect(address);
            }
        }

        public void kill() throws IOException {
            if (streamConn != null) {
                streamConn.close();
            }
            if (streamListen != null) {
                streamListen.close();
            }
            if (dgram != null) {
                dgram.close();
            }
        }

        public void listen(int backlog) throws IOException {
            if (kind == SOCK_STREAM) {
                streamListen().bind(bindAddress, backlog);
            }
        }

        public int receive(byte[] buf, int len) throws IOException {
            switch (kind) {
                case SOCK_STREAM -> {
                    if (streamConn == null) {
                        throw new IOException("Socket not connected");
                    }
                    val in = streamConn.getInputStream();
                    val read = in.read(buf);
                    return read;
                }
                case SOCK_DGRAM -> {
                    val packet = new DatagramPacket(buf, len);
                    dgram().receive(packet);
                    return packet.getLength();
                }
            }
            throw new IllegalStateException("Invalid socket type");
        }

        public int send(byte[] data) throws IOException {
            switch (kind) {
                case SOCK_STREAM -> {
                    if (streamConn == null) {
                        throw new IOException("Socket not connected");
                    }
                    OutputStream out;
                    out = streamConn.getOutputStream();
                    out.write(data);
                    return data.length;
                }
                case SOCK_DGRAM -> {
                    val packet = new DatagramPacket(data, data.length);
                    if (dgram == null) {
                        return 0;
                    }
                    dgram.send(packet);
                    return data.length;
                }
            }
            return 0;
        }

        public int available() throws IOException {
            if (kind == SOCK_STREAM) {
                if (streamConn == null) {
                    throw new IOException("Socket not connected");
                }
                return streamConn.getInputStream().available();
            }
            throw new IOException("Invalid socket type");
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SCKE implements Fingerprint {
        public static final SCKE INSTANCE = new SCKE();

        @Instr('H')
        public static void parseDomainName(ExecutionContext ctx) {
            val domainName = ctx.stack().popString();
            try {
                val ip = InetAddress.getByName(domainName);
                val ipBytes = ip.getAddress();
                var ipInt = 0;
                for (byte ipByte : ipBytes) {
                    ipInt = ipInt * 256 + (ipByte & 0xFF);
                }
                ctx.stack().push(ipInt);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                ctx.IP().reflect();
            }
        }

        @Instr('P')
        public static void checkAvailable(ExecutionContext ctx) {
            val s = ctx.stack().pop();
            val socket = sockets.get(s);
            if (socket == null) {
                ctx.IP().reflect();
                return;
            }
            try {
                val available = socket.streamConn().getInputStream().available();
                ctx.stack().push(available);
            } catch (IOException e) {
                e.printStackTrace();
                ctx.IP().reflect();
            }
        }

        @Override
        public int code() {
            return 0x53434B45;
        }
    }
}
