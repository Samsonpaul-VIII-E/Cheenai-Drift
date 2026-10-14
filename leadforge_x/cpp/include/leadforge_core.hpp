#pragma once
#include <string>
#include <vector>

namespace leadforge {
std::string fingerprint(const std::string& input);
double keyword_score(const std::string& text, const std::vector<std::string>& keywords);
std::size_t unique_count(const std::vector<std::string>& values);
}
