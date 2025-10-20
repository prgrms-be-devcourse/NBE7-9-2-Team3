'use client';

import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { User, LoginRequest, SignupRequest, LoginResponse, SignupResponse, ApiResponse } from '@/type/user';
import { fetchApi } from '@/lib/client';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  signup: (email: string, password: string, nickname: string, profileImage?: File) => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const isAuthenticated = !!user;

  // 컴포넌트 마운트 시 사용자 정보 확인
  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const data: ApiResponse<User> = await fetchApi('/api/members/me');
      if (data.data) {
        setUser(data.data);
      }
    } catch (error) {
      // 로그인하지 않은 상태에서는 조용히 처리
      if (error instanceof Error && (
        error.message.includes('Failed to fetch') ||
        error.message.includes('HTTP 401') ||
        error.message.includes('HTTP 403') ||
        error.message.includes('HTTP 404') ||
        error.message.includes('Access Token')
      )) {
        // 로그인하지 않은 상태이므로 조용히 처리
        console.log('사용자가 로그인하지 않았습니다.');
      } else {
        console.error('Auth check failed:', error);
      }
    } finally {
      setLoading(false);
    }
  };


  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const loginData: LoginRequest = { email, password };
      const data: ApiResponse<LoginResponse> = await fetchApi('/api/members/login', {
        method: 'POST',
        body: JSON.stringify(loginData),
      });

      if (data.data) {
        setUser({
          memberId: data.data.memberId,
          email: data.data.email,
          nickname: data.data.nickname,
          profileImage: data.data.profileImage,
        });
      }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const signup = async (email: string, password: string, nickname: string, profileImage?: File) => {
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append('email', email.trim());
      formData.append('password', password.trim());
      formData.append('nickname', nickname.trim());
      if (profileImage) {
        formData.append('profileImageFile', profileImage);
      }
      
      const data: ApiResponse<SignupResponse> = await fetchApi('/api/members/join', {
        method: 'POST',
        body: formData,
      });

      if (data.data) {
        setUser({
          memberId: data.data.memberId,
          email: data.data.email,
          nickname: data.data.nickname,
          profileImage: data.data.profileImage,
        });
      }
    } catch (error) {
      console.error('Signup error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await fetchApi('/api/members/logout', {
        method: 'POST',
      });
    } catch (error) {
      // 로그아웃 에러는 조용히 처리 (이미 토큰이 없거나 만료된 경우)
      if (error instanceof Error && error.message.includes('Access Token')) {
        console.log('이미 로그아웃 상태입니다.');
      } else {
        console.error('Logout error:', error);
      }
    } finally {
      setUser(null);
      setLoading(false);
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated,
    login,
    logout,
    signup,
    loading,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
