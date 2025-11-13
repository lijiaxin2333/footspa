//
//  FootspaTabView.swift
//  footspa
//
//  Created by ljx on 2025/11/13.
//

import SwiftUI

struct FootspaTabView: View {
    var body: some View {
        TabView {
            NavigationStack {
                CardTypeManagementView()
            }.tabItem {
                Label("首页", systemImage: "house")
            }
            NavigationStack {
                
            }.tabItem {
                Label("我的", systemImage: "person")
            }
        }
    }
}

#Preview {
    FootspaTabView()
}
