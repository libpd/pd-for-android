/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/peter/Documents/pd-for-android-common/src/org/puredata/android/service/IPdClient.aidl
 */
package org.puredata.android.service;
public interface IPdClient extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.puredata.android.service.IPdClient
{
private static final java.lang.String DESCRIPTOR = "org.puredata.android.service.IPdClient";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.puredata.android.service.IPdClient interface,
 * generating a proxy if needed.
 */
public static org.puredata.android.service.IPdClient asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.puredata.android.service.IPdClient))) {
return ((org.puredata.android.service.IPdClient)iin);
}
return new org.puredata.android.service.IPdClient.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_audioChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
float _arg3;
_arg3 = data.readFloat();
this.audioChanged(_arg0, _arg1, _arg2, _arg3);
return true;
}
case TRANSACTION_requestUnbind:
{
data.enforceInterface(DESCRIPTOR);
this.requestUnbind();
return true;
}
case TRANSACTION_print:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.print(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.puredata.android.service.IPdClient
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
	 * announce a (re)start of the audio thread to client; a sample rate of 0 means audio was stopped
	 */
public void audioChanged(int sampleRate, int nIn, int nOut, float bufferSizeMillis) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sampleRate);
_data.writeInt(nIn);
_data.writeInt(nOut);
_data.writeFloat(bufferSizeMillis);
mRemote.transact(Stub.TRANSACTION_audioChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
/**
	 * ask client to unbind from service
	 */
public void requestUnbind() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_requestUnbind, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
/**
	 * print from pd
	 */
public void print(java.lang.String s) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(s);
mRemote.transact(Stub.TRANSACTION_print, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_audioChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_requestUnbind = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_print = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
	 * announce a (re)start of the audio thread to client; a sample rate of 0 means audio was stopped
	 */
public void audioChanged(int sampleRate, int nIn, int nOut, float bufferSizeMillis) throws android.os.RemoteException;
/**
	 * ask client to unbind from service
	 */
public void requestUnbind() throws android.os.RemoteException;
/**
	 * print from pd
	 */
public void print(java.lang.String s) throws android.os.RemoteException;
}
