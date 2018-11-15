# -*- coding:utf-8 -*-
import socket

def connect_to_server(id):

    # 发送本机的硬件id，之后不能向服务器发送任何数据了，只能接收服务器传来的数据#
    inte=str(id)
    obj =  socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    obj.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)

    obj.connect(("127.0.0.1",9999))
    obj.sendall((inte+"\n").encode())
    while True:
        ret_bytes = obj.recv(1024)
        ret_str = str(ret_bytes,encoding="utf-8")
        #这儿可以根据服务器返回的不同的数来调用不同的方法，从而实现远程控制#
        if ret_str.__eq__("print1"):
            print1()
        if ret_str.__eq__("print2"):
            print2()


def print1():
    print("1")

def print2():
    print2("2")


if __name__ == '__main__':
    # size = get_dir_size(r'C:\Users\55juekun\Documents\Tencent Files\1061229681\FileRecv\RailwayMonitorServer')
    # print('There are %.3f' % (size / 1024 / 1024), 'Mbytes ')
    localid=111
    connect_to_server(localid)
