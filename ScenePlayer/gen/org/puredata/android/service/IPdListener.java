/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/peter/Documents/pd-for-android-common/src/org/puredata/android/service/IPdListener.aidl
 */
package org.puredata.android.service;
public interface IPdListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.puredata.android.service.IPdListener
{
private static final java.lang.String DESCRIPTOR = "org.puredata.android.service.IPdListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.puredata.android.service.IPdListener interface,
 * generating a proxy if needed.
 */
public static org.puredata.android.service.IPdListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.puredata.android.service.IPdListener))) {
return ((org.puredata.android.service.IPdListener)iin);
}
return new org.puredata.android.service.IPdListener.Stub.Proxy(obj);
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
case TRANSACTION_receiveBang:
{
data.enforceInterface(DESCRIPTOR);
this.receiveBang();
return true;
}
case TRANSACTION_receiveFloat:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
this.receiveFloat(_arg0);
return true;
}
case TRANSACTION_receiveSymbol:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.receiveSymbol(_arg0);
return true;
}
case TRANSACTION_receiveList:
{
data.enforceInterface(DESCRIPTOR);
java.util.List _arg0;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg0 = data.readArrayList(cl);
this.receiveList(_arg0);
return true;
}
case TRANSACTION_receiveMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readArrayList(cl);
this.receiveMessage(_arg0, _arg1);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.puredata.android.service.IPdListener
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
public void receiveBang() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_receiveBang, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void receiveFloat(float x) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(x);
mRemote.transact(Stub.TRANSACTION_receiveFloat, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void receiveSymbol(java.lang.String symbol) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(symbol);
mRemote.transact(Stub.TRANSACTION_receiveSymbol, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void receiveList(java.util.List args) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeList(args);
mRemote.transact(Stub.TRANSACTION_receiveList, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void receiveMessage(java.lang.String symbol, java.util.List args) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(symbol);
_data.writeList(args);
mRemote.transact(Stub.TRANSACTION_receiveMessage, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_receiveBang = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_receiveFloat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_receiveSymbol = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_receiveList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_receiveMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void receiveBang() throws android.os.RemoteException;
public void receiveFloat(float x) throws android.os.RemoteException;
public void receiveSymbol(java.lang.String symbol) throws android.os.RemoteException;
public void receiveList(java.util.List args) throws android.os.RemoteException;
public void receiveMessage(java.lang.String symbol, java.util.List args) throws android.os.RemoteException;
}
